package com.creati.ui.main;

import com.creati.model.LogPost;
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

    // 키는 프로젝트 루트 .env 파일에서 로드 (소스코드에 직접 작성 X)
    private static final String GEMINI_API_KEY =
            EnvLoader.get("GEMINI_API_KEY");
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

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
        String content = callGemini(type, log);

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

    // ─── Gemini API 호출 ───────────────────────────
    private String callGemini(AiAnalysisRecord.Type type, LogPost log) {
        if (GEMINI_API_KEY == null || GEMINI_API_KEY.isBlank()) {
            System.err.println("[AiAnalysisService] GEMINI_API_KEY 미설정 → 스텁 반환");
            return buildStubContent(type, log);
        }
        try {
            String escaped = buildPrompt(type, log)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String jsonBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + GEMINI_API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resp =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                System.err.println("[AiAnalysisService] Gemini 오류 " + resp.statusCode());
                return buildStubContent(type, log);
            }
            return formatGeminiResponse(resp.body());

        } catch (Exception e) {
            System.err.println("[AiAnalysisService] Gemini 예외: " + e.getMessage());
            return buildStubContent(type, log);
        }
    }

    private String formatGeminiResponse(String rawJson) {
        try {
            String marker = "\"text\": \"";
            int start = rawJson.indexOf(marker);
            if (start < 0) return "AI 응답을 파싱하지 못했어요.";
            start += marker.length();
            int end = start;
            while (end < rawJson.length()) {
                char c = rawJson.charAt(end);
                if (c == '"' && end > 0 && rawJson.charAt(end - 1) != '\\') break;
                end++;
            }
            String text = rawJson.substring(start, end)
                    .replace("\\n", "\n").replace("\\\"", "\"")
                    .replace("\\\\", "\\").replace("**", "");
            StringBuilder sb = new StringBuilder();
            for (String line : text.split("\n")) {
                String t = line.trim();
                if (t.isEmpty()) continue;
                sb.append(t.startsWith("-") || t.startsWith("•") ? "  " : "")
                  .append(t).append("\n\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "AI 응답 처리 중 오류가 발생했어요.";
        }
    }

    // ─── 프롬프트 생성 ─────────────────────────────
    private String buildPrompt(AiAnalysisRecord.Type type, LogPost log) {
        String context =
                "[성장 로그 정보]\n"
                + "제목: "        + titleOf(log) + "\n"
                + "목표: "        + s(log == null ? null : log.goalText) + "\n"
                + "과정: "        + s(log == null ? null : log.processText) + "\n"
                + "배운 점: "     + s(log == null ? null : log.learningText) + "\n"
                + "아쉬운 점: "   + s(log == null ? null : log.painPoint) + "\n"
                + "다음 시도 조건: " + s(log == null ? null : log.retryCondition) + "\n"
                + "기분/평가: "   + s(log == null ? null : log.mood) + "\n";

        String instruction = switch (type) {
            case CAUSE ->
                    "너는 숙련된 크리에이터 컨설턴트 '에티'야.\n"
                    + "위 성장 로그를 읽고, 시도가 잘 안 된 핵심 원인을 분석해줘.\n"
                    + "- 핵심 원인 2~3가지\n- 각 원인의 근거 (로그 내용 기반)\n- 한 줄 개선 제안\n"
                    + "친근하고 따뜻하게, 한국어로 답해줘.";
            case RETRO ->
                    "너는 숙련된 크리에이터 컨설턴트 '에티'야.\n"
                    + "위 성장 로그를 읽고 회고를 정리해줘.\n"
                    + "- 잘한 점 2가지 이상\n- 아쉬운 점 2가지 이상\n- 다음 행동 제안\n"
                    + "친근하고 따뜻하게, 한국어로 답해줘.";
            case RETRY ->
                    "너는 숙련된 크리에이터 컨설턴트 '에티'야.\n"
                    + "위 성장 로그를 읽고 재도전 방향을 알려줘.\n"
                    + "- 다음 목표 (구체적)\n- 체크 포인트\n- 실패 대비 플랜\n"
                    + "친근하고 따뜻하게, 한국어로 답해줘.";
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