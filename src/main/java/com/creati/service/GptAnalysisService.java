package com.creati.service;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class GptAnalysisService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    /**
     * 이번 달 해당 유저의 AI 피드백을 DB에서 가져와 Gemini로 월간 인사이트를 생성합니다.
     */
    public String analyzeMonthlyInsight(String userId) throws Exception {
        com.creati.dao.AiAnalysisDao dao = new com.creati.dao.AiAnalysisDao();
        java.util.List<com.creati.dao.AiAnalysisDao.AiAnalysisRow> rows = dao.findThisMonthByUserId(userId);

        if (rows.isEmpty()) {
            return "이번 달에 아직 AI 피드백 기록이 없어요.\n로그를 작성하고 AI 분석을 받은 뒤 다시 시도해보세요!";
        }

        // 피드백들을 하나의 텍스트로 합치기
        StringBuilder feedbackSummary = new StringBuilder();
        for (com.creati.dao.AiAnalysisDao.AiAnalysisRow row : rows) {
            feedbackSummary.append("[").append(row.aType).append("] ");
            if (row.aTitle != null && !row.aTitle.isBlank()) {
                feedbackSummary.append(row.aTitle).append(": ");
            }
            feedbackSummary.append(row.aContent).append("\n\n");
        }

        String prompt = "너는 숙련된 크리에이터 컨설턴트야. "
                + "아래는 한 크리에이터가 이번 달 받은 AI 피드백 목록이야.\n\n"
                + feedbackSummary
                + "\n위 피드백들을 바탕으로 이번 달의 성장 패턴을 분석하고, "
                + "다음 달에 집중해야 할 핵심 포인트 1가지를 구체적으로 제안해줘. "
                + "따뜻하고 응원하는 톤으로, 3~4문단으로 작성해줘.";

        HttpClient client = HttpClient.newHttpClient();
        String escaped = prompt.replace("\\", "\\\\").replace("\"", "\\\"")
                               .replace("\n", "\\n").replace("\r", "");
        String jsonBody = "{\"contents\": [{\"parts\":[{\"text\": \"" + escaped + "\"}]}]}";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return formatResponse(response.body());
    }

    public String analyzeDummy() throws Exception {
        // 하위 호환용 더미 메서드 (미사용)
        String prompt = "너는 숙련된 크리에이터 컨설턴트야. 사용자가 유튜브 쇼츠 채널 성장이 더뎌서 고민이야. "
                      + "실패 원인 카테고리, 요약, 재도전 플랜을 포함해서 응답해줘.";
        
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"contents\": [{\"parts\":[{\"text\": \"" + prompt + "\"}]}]}";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return formatResponse(response.body());
    }

    private String formatResponse(String response) {
        try {
            // 1. "text": " 시작 위치 찾기
            String searchTarget = "\"text\": \"";
            int startPos = response.indexOf(searchTarget);
            if (startPos < 0) return "응답 파싱 실패: text 필드를 찾을 수 없어요.";
            startPos += searchTarget.length();

            // 2. 끝 따옴표 찾기 - \" (이스케이프된 따옴표)는 건너뛰고 진짜 닫는 " 를 찾음
            int endPos = startPos;
            while (endPos < response.length()) {
                char c = response.charAt(endPos);
                if (c == '\\') {
                    endPos += 2; // 이스케이프 시퀀스 skip (\n, \", \\ 등)
                    continue;
                }
                if (c == '"') break; // 진짜 닫는 따옴표
                endPos++;
            }

            String result = response.substring(startPos, endPos);

            // 3. 이스케이프 시퀀스 복원
            result = result.replace("\\n", "\n")
                           .replace("\\r", "")
                           .replace("\\\"", "\"")
                           .replace("\\\\", "\\");

            // 4. 마크다운 강조 제거
            result = result.replace("**", "").replace("##", "").replace("# ", "");

            // 5. 줄 정리
            StringBuilder cleaned = new StringBuilder();
            for (String line : result.split("\n")) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    if (trimmedLine.startsWith("-")) {
                        cleaned.append("  ").append(trimmedLine).append("\n\n");
                    } else {
                        cleaned.append(trimmedLine).append("\n\n");
                    }
                }
            }

            return cleaned.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "데이터 가공 중 오류 발생";
        }
    }
}