package com.creati.service;

import com.creati.util.EnvLoader;

import java.net.URI;
import java.net.http.*;

public class GptAnalysisService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL   = "claude-haiku-4-5-20251001";

    private static String getApiKey() {
        return EnvLoader.get("ANTHROPIC_API_KEY");
    }

    /**
     * 이번 달 해당 유저의 AI 피드백을 DB에서 가져와 Claude로 월간 인사이트를 생성합니다.
     */
    public String analyzeMonthlyInsight(String userId) throws Exception {
        com.creati.dao.AiAnalysisDao dao = new com.creati.dao.AiAnalysisDao();
        java.util.List<com.creati.dao.AiAnalysisDao.AiAnalysisRow> rows = dao.findThisMonthByUserId(userId);

        if (rows.isEmpty()) {
            return "이번 달에 아직 AI 피드백 기록이 없어요.\n로그를 작성하고 AI 분석을 받은 뒤 다시 시도해보세요!";
        }

        StringBuilder feedbackSummary = new StringBuilder();
        for (com.creati.dao.AiAnalysisDao.AiAnalysisRow row : rows) {
            feedbackSummary.append("[").append(row.aType).append("] ");
            if (row.aTitle != null && !row.aTitle.isBlank()) {
                feedbackSummary.append(row.aTitle).append(": ");
            }
            feedbackSummary.append(row.aContent).append("\n\n");
        }

        String prompt = "아래는 한 크리에이터가 이번 달 받은 AI 피드백 목록이야.\n\n"
                + feedbackSummary
                + "\n위 피드백들을 바탕으로 이번 달의 성장 패턴을 분석하고, "
                + "다음 달에 집중해야 할 핵심 포인트 1가지를 구체적으로 제안해줘. "
                + "따뜻하고 응원하는 톤으로, 3~4문단으로 작성해줘. "
                + "마크다운 기호(#, **, --- 등)는 사용하지 말고 일반 텍스트로만 작성해줘.";

        return callClaude(prompt);
    }

    public String analyzeDummy() throws Exception {
        String prompt = "너는 숙련된 크리에이터 컨설턴트야. 사용자가 유튜브 쇼츠 채널 성장이 더뎌서 고민이야. "
                      + "실패 원인, 요약, 재도전 플랜을 포함해서 따뜻하게 응답해줘. "
                      + "마크다운 기호(#, **, --- 등)는 사용하지 말고 일반 텍스트로만 작성해줘.";
        return callClaude(prompt);
    }

    private String callClaude(String userPrompt) throws Exception {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[GptAnalysisService] ANTHROPIC_API_KEY 미설정");
            return "API 키가 설정되지 않았어요. .env 파일에 ANTHROPIC_API_KEY를 추가해 주세요.";
        }

        String system = "너는 숙련된 크리에이터 컨설턴트야. 친근하고 따뜻하게, 한국어로 답해줘.";

        String jsonBody = "{"
                + "\"model\": \"" + CLAUDE_MODEL + "\","
                + "\"max_tokens\": 1024,"
                + "\"system\": \"" + escapeJson(system) + "\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}]"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[GptAnalysisService] Claude 응답 코드: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.err.println("[GptAnalysisService] Claude 오류: " + response.body());
            return "AI 응답 중 오류가 발생했어요 (코드: " + response.statusCode() + ")";
        }

        return parseResponse(response.body());
    }

    private String parseResponse(String rawJson) {
        try {
            // 공백 있는/없는 둘 다 처리
            String marker = "\"text\":\"";
            int start = rawJson.indexOf(marker);
            if (start < 0) {
                marker = "\"text\": \"";
                start = rawJson.indexOf(marker);
            }
            if (start < 0) {
                System.err.println("[GptAnalysisService] 파싱 실패: " + rawJson);
                return "응답 파싱 실패: text 필드를 찾을 수 없어요.";
            }
            start += marker.length();

            int end = start;
            while (end < rawJson.length()) {
                char c = rawJson.charAt(end);
                if (c == '\\') { end += 2; continue; }
                if (c == '"') break;
                end++;
            }

            String text = rawJson.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\r", "")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("**", "")
                    .replace("##", "")
                    .replace("# ", "")
                    .replaceAll("[\ud83c\udf00-\ud83d\udde7]", "")
                    .replaceAll("[\ud83d\udde8-\ud83e\uddff]", "")
                    .replaceAll("[\u2600-\u27BF]", "");

            StringBuilder sb = new StringBuilder();
            for (String line : text.split("\n")) {
                String t = line.trim();
                if (t.isEmpty()) continue;
                if (t.startsWith("-")) {
                    sb.append("  · ").append(t.substring(1).trim()).append("\n\n");
                } else if (t.equals("---")) {
                    sb.append("\n");
                } else {
                    sb.append(t).append("\n\n");
                }
            }
            return sb.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "데이터 가공 중 오류 발생";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}