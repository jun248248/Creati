package com.creati.service;

import com.creati.model.AiAnalysisRecord;
import com.creati.model.AiAnalysisStore;
import com.creati.model.LogPost;
import com.creati.model.AiAnalysisRecord.Type;
import com.creati.ui.main.Services;
import com.creati.util.EnvLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AiAnalysisService {

    // Claude API 엔드포인트 및 모델 (키는 호출마다 동적으로 읽음)
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL   = "claude-haiku-4-5-20251001"; // 빠르고 저렴한 모델

    private static String getApiKey() {
        return EnvLoader.get("ANTHROPIC_API_KEY");
    }

    private final AiAnalysisStore store;

    public AiAnalysisService(AiAnalysisStore store) {
        this.store = Objects.requireNonNull(store);
    }

    // ─── 조회 / 존재 확인 ───────────────────────────
    public boolean isAnalyzed(String logId) {
        return store.isAnalyzed(logId);
    }

    public boolean isTypeAnalyzed(String logId, AiAnalysisRecord.Type type) {
        return store.hasType(logId, type);
    }

    public List<AiAnalysisRecord> listRecords(String logId) {
        if (logId == null || logId.isBlank()) return List.of();
        return store.listByLogId(logId);
    }

    public LogPost findMostRecentLog() {
        List<LogPost> all = Services.LOGS.list();
        if (all == null || all.isEmpty()) return null;
        return all.stream()
                .filter(p -> p != null && LogPost.TYPE_LOG.equals(p.type))
                .max(Comparator
                        .comparing((LogPost p) -> p.createdAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(p -> p.id == null ? "" : p.id))
                .orElse(null);
    }

    // ─── 미리보기 (DB 저장 X) ──────────────────────
    public AiAnalysisRecord preview(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || logId.isBlank()) throw new IllegalArgumentException("logId is required");
        if (type == null) throw new IllegalArgumentException("type is required");

        LogPost log = Services.LOGS.getById(logId);
        String content = callClaude(type, log);

        return new AiAnalysisRecord(
                "air_preview_" + UUID.randomUUID(),
                logId,
                type,
                titleOf(log) + " \u00b7 " + type.label,
                LocalDate.now(),
                content
        );
    }

    // ─── 저장 (DB INSERT) ──────────────────────────
    public AiAnalysisRecord save(String logId, AiAnalysisRecord.Type type, String content) {
        if (logId == null || logId.isBlank()) throw new IllegalArgumentException("logId is required");
        if (type == null) throw new IllegalArgumentException("type is required");
        if (store.hasType(logId, type)) throw new IllegalStateException("ALREADY_ANALYZED_TYPE");

        LogPost log = Services.LOGS.getById(logId);
        AiAnalysisRecord r = new AiAnalysisRecord(
                "air_" + UUID.randomUUID(),
                logId,
                type,
                titleOf(log) + " \u00b7 " + type.label,
                LocalDate.now(),
                content == null ? "" : content
        );
        store.add(r);
        return r;
    }

    public AiAnalysisRecord analyze(String logId, AiAnalysisRecord.Type type) {
        AiAnalysisRecord p = preview(logId, type);
        return save(logId, type, p.content);
    }

    // ─── 자유 질문 (채팅 입력창) ───────────────────
    public String askFreeText(String logId, String question) {
        if (question == null || question.isBlank()) return "질문을 입력해 주세요.";

        LogPost log = (logId != null && !logId.isBlank()) ? Services.LOGS.getById(logId) : null;

        String context = (log != null)
                ? "[성장 로그 정보]\n"
                  + "제목: "           + titleOf(log) + "\n"
                  + "목표: "           + s(log.goalText) + "\n"
                  + "과정: "           + s(log.processText) + "\n"
                  + "배운 점: "        + s(log.learningText) + "\n"
                  + "아쉬운 점: "      + s(log.painPoint) + "\n"
                  + "다음 시도 조건: " + s(log.retryCondition) + "\n"
                  + "기분/평가: "      + s(log.mood) + "\n\n"
                : "";

        String prompt = context + "사용자 질문: " + question + "\n친근하고 따뜻하게, 구체적으로 한국어로 답해줘.";
        String system = "너는 숙련된 크리에이터 컨설턴트 '에티'야.";

        return callClaudeRaw(system, prompt, "AI 연결 중 오류가 발생했어요. 네트워크 상태를 확인해 주세요.");
    }

    // ─── Claude API 호출 (버튼 분석용) ────────────
    private String callClaude(AiAnalysisRecord.Type type, LogPost log) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[AiAnalysisService] ANTHROPIC_API_KEY 미설정 → 스텁 반환");
            return buildStubContent(type, log);
        }

        String system = "너는 숙련된 크리에이터 컨설턴트 '에티'야. 친근하고 따뜻하게, 한국어로 답해줘.";
        String prompt = buildPrompt(type, log);

        try {
            String result = callClaudeRaw(system, prompt, null);
            if (result == null) return buildStubContent(type, log);
            return result;
        } catch (Exception e) {
            System.err.println("[AiAnalysisService] Claude 예외: " + e.getMessage());
            return buildStubContent(type, log);
        }
    }

    // ─── Claude API 공통 호출 ──────────────────────
    private String callClaudeRaw(String system, String userPrompt, String fallback) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[AiAnalysisService] ANTHROPIC_API_KEY 미설정");
            return fallback != null ? fallback : "API 키가 설정되지 않았어요. .env 파일에 ANTHROPIC_API_KEY를 추가해 주세요.";
        }

        try {
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

            HttpResponse<String> resp =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[AiAnalysisService] Claude 응답 코드: " + resp.statusCode());

            if (resp.statusCode() != 200) {
                System.err.println("[AiAnalysisService] Claude 오류 " + resp.statusCode() + " → " + resp.body());
                return fallback != null ? fallback : "AI 응답 중 오류가 발생했어요 (코드: " + resp.statusCode() + ")";
            }

            return parseClaudeResponse(resp.body());

        } catch (Exception e) {
            System.err.println("[AiAnalysisService] Claude 예외: " + e.getMessage());
            return fallback != null ? fallback : "AI 연결 중 오류가 발생했어요.";
        }
    }

    // ─── Claude 응답 JSON 파싱 + 마크다운 → 읽기 좋은 텍스트 변환 ──
    private String parseClaudeResponse(String rawJson) {
        try {
            // 공백 있는/없는 둘 다 처리
            String marker = "\"text\":\"";
            int start = rawJson.indexOf(marker);
            if (start < 0) {
                marker = "\"text\": \"";
                start = rawJson.indexOf(marker);
            }
            if (start < 0) {
                System.err.println("[AiAnalysisService] 파싱 실패, 원본: " + rawJson);
                return "AI 응답을 파싱하지 못했어요.";
            }
            start += marker.length();
            int end = start;
            while (end < rawJson.length()) {
                char c = rawJson.charAt(end);
                if (c == '"' && rawJson.charAt(end - 1) != '\\') break;
                end++;
            }
            String text = rawJson.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            return formatMarkdown(removeEmoji(text));

        } catch (Exception e) {
            System.err.println("[AiAnalysisService] 파싱 예외: " + e.getMessage());
            return "AI 응답 처리 중 오류가 발생했어요.";
        }
    }

    // ─── 이모지 제거 ────────────────────────────
    private String removeEmoji(String text) {
        // 이모지 범위 제거 (Java unicode 정규식)
        return text.replaceAll("[\ud83c\udf00-\ud83d\udde7]", "")
                   .replaceAll("[\ud83d\udde8-\ud83e\uddff]", "")
                   .replaceAll("[\u2600-\u27BF]", "")
                   .replaceAll("[\uFE00-\uFE0F]", "")
                   .replaceAll("\u200D", "")
                   .replaceAll("  +", " ")
                   .trim();
    }

    // ─── 마크다운 → 읽기 좋은 텍스트 변환 ────────
    private String formatMarkdown(String text) {
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String t = lines[i].trim();
            if (t.isEmpty()) continue;

            // ### 소제목 → 【 】 형태
            if (t.startsWith("### ")) {
                String title = t.substring(4).replaceAll("\\*\\*|\\*|#", "").trim();
                // 이모지 포함된 제목이면 그대로, 아니면 ▸ 추가
                sb.append("\n【 ").append(title).append(" 】\n");

            // ## 중제목 → ─── 형태
            } else if (t.startsWith("## ")) {
                String title = t.substring(3).replaceAll("\\*\\*|\\*|#", "").trim();
                sb.append("\n").append(title).append("\n");

            // # 대제목
            } else if (t.startsWith("# ")) {
                String title = t.substring(2).replaceAll("\\*\\*|\\*|#", "").trim();
                sb.append("\n◆ ").append(title).append("\n");

            // --- 구분선 → 공백으로
            } else if (t.equals("---") || t.equals("─────────────────────")) {
                sb.append("\n");

            // - 또는 • 리스트 항목
            } else if (t.startsWith("- ") || t.startsWith("• ")) {
                String item = t.substring(2).replaceAll("\\*\\*([^*]+)\\*\\*", "$1").trim();
                sb.append("  · ").append(item).append("\n");

            // 숫자 리스트 1. 2. 3.
            } else if (t.matches("^\\d+\\.\\s.*")) {
                String item = t.replaceAll("\\*\\*([^*]+)\\*\\*", "$1").trim();
                sb.append("  ").append(item).append("\n");

            // 일반 텍스트 - ** 볼드 제거
            } else {
                String clean = t.replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                                .replaceAll("\\*([^*]+)\\*", "$1")
                                .replace("#", "").trim();
                if (!clean.isEmpty()) {
                    sb.append(clean).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }

    // ─── JSON 문자열 이스케이프 ────────────────────
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ─── 프롬프트 생성 ─────────────────────────────
    private String buildPrompt(AiAnalysisRecord.Type type, LogPost log) {
        String context =
                "[성장 로그 정보]\n"
                + "제목: "           + titleOf(log) + "\n"
                + "목표: "           + s(log == null ? null : log.goalText) + "\n"
                + "과정: "           + s(log == null ? null : log.processText) + "\n"
                + "배운 점: "        + s(log == null ? null : log.learningText) + "\n"
                + "아쉬운 점: "      + s(log == null ? null : log.painPoint) + "\n"
                + "다음 시도 조건: " + s(log == null ? null : log.retryCondition) + "\n"
                + "기분/평가: "      + s(log == null ? null : log.mood) + "\n";

        String instruction = switch (type) {
            case CAUSE ->
                    "위 성장 로그를 읽고, 시도가 잘 안 된 핵심 원인을 분석해줘.\n"
                    + "- 핵심 원인 2~3가지\n- 각 원인의 근거 (로그 내용 기반)\n- 한 줄 개선 제안";
            case RETRO ->
                    "위 성장 로그를 읽고 회고를 정리해줘.\n"
                    + "- 잘한 점 2가지 이상\n- 아쉬운 점 2가지 이상\n- 다음 행동 제안";
            case RETRY ->
                    "위 성장 로그를 읽고 재도전 방향을 알려줘.\n"
                    + "- 다음 목표 (구체적)\n- 체크 포인트\n- 실패 대비 플랜";
        };
        return context + "\n" + instruction;
    }

    // ─── 스텁 (키 없을 때 대체 텍스트) ────────────
    private String buildStubContent(AiAnalysisRecord.Type type, LogPost log) {
        String title = titleOf(log);
        return switch (type) {
            case CAUSE ->
                    "[원인 분석 · 임시 결과]\n"
                    + "- 핵심 원인: 목표/기준이 명확하지 않아 시도가 흔들렸을 수 있어요.\n"
                    + "- 증거: 중간에 기준이 바뀌거나 시간 배분이 일정하지 않았어요.\n"
                    + "- 다음 한 줄: 범위를 더 줄여볼까요?\n\n(분석 대상: " + title + ")";
            case RETRO ->
                    "[회고 정리 · 임시 결과]\n"
                    + "- 잘한 점: 꾸준히 시도했고 기록이 남아 있어요.\n"
                    + "- 아쉬운 점: 범위를 줄이지 못해 피로가 누적됐을 수 있어요.\n"
                    + "- 다음 행동: 다음엔 결론 1줄을 먼저 쓰고 본문을 채워보세요.\n\n(분석 대상: " + title + ")";
            case RETRY ->
                    "[재도전 방향 · 임시 결과]\n"
                    + "- 다음 목표: 7일 동안 하루 10분 루틴으로 축소해요.\n"
                    + "- 체크 포인트: 3일째에 한 번만 개선하고 나머지는 유지!\n"
                    + "- 실패 대비: 못 한 날은 0분 대신 1분으로라도 연결하기.\n\n(분석 대상: " + title + ")";
        };
    }

    // ─── 유틸 ──────────────────────────────────────
    private String titleOf(LogPost log) {
        return (log == null || log.title == null || log.title.isBlank()) ? "(제목 없음)" : log.title;
    }

    private String s(String v) {
        return (v == null || v.isBlank()) ? "(미입력)" : v.trim();
    }
}