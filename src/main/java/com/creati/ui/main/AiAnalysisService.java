package com.creati.ui.main;

import com.creati.model.LogPost;
import com.creati.service.GptAnalysisService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// API(TODO): Replace stub analysis with real API call.
// DB(TODO): Replace in-memory store with repository.

public class AiAnalysisService {

    private final AiAnalysisStore store;
    private final GptAnalysisService gptService = new GptAnalysisService();
    
    public AiAnalysisService(AiAnalysisStore store) {
        this.store = Objects.requireNonNull(store);
    }

    
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
        return Services.LOGS.list().stream()
            .filter(p -> p != null && "LOG".equals(p.type))
            .max(Comparator.comparing(p -> p.createdAt))
            .orElse(null);
    }

    // API(TODO): Preview analysis result (does NOT save). TODO(AI API): 여기서 실제 AI API 호출 결과로 content를 채우도록 교체 
    public AiAnalysisRecord preview(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || logId.isBlank()) throw new IllegalArgumentException("logId is required");
        if (type == null) throw new IllegalArgumentException("type is required");

        LogPost log = Services.LOGS.getById(logId);
        String logTitle = (log == null || log.title == null || log.title.isBlank()) ? "(제목 없음)" : log.title;

        String content = buildStubContent(type, log);

        return new AiAnalysisRecord(
                "air_preview_" + UUID.randomUUID(),
                logId,
                type,
                logTitle + " · " + type.label,
                LocalDate.now(),
                content
        );
    }

    
    public AiAnalysisRecord save(String logId, AiAnalysisRecord.Type type, String content) {
        AiAnalysisRecord r = new AiAnalysisRecord(
            UUID.randomUUID().toString(), logId, type, "에티의 " + type.label, LocalDate.now(), content
        );
        store.add(r);
        return r;
    }

    
    public AiAnalysisRecord analyze(String logId, AiAnalysisRecord.Type type) {
        AiAnalysisRecord preview = preview(logId, type);
        return save(logId, type, preview.content);
    }

    
    public String requestAiAnalysis(String logId, AiAnalysisRecord.Type type) throws Exception {
        LogPost log = Services.LOGS.getById(logId);
        if (log == null) return "로그를 찾을 수 없습니다.";

        // 프롬프트 구성 (UI 수정 없이 내부 로직만 수행)
        String prompt = String.format(
            "너는 크리에이터 컨설턴트 에티야. 제목: [%s], 내용: [%s]. [%s] 관점에서 분석해줘.",
            log.title, log.whatIDid, type.label
        );
        return gptService.analyzeWithPrompt(prompt);
    }
    

    private String buildStubContent(AiAnalysisRecord.Type type, LogPost log) {
        String title = (log == null || log.title == null || log.title.isBlank()) ? "(제목 없음)" : log.title;

        return switch (type) {
            case CAUSE -> "[원인 분석 · 임시 결과]\n"
                    + "- 핵심 원인: (예) 목표/기준이 명확하지 않아 시도가 흔들렸을 수 있어요.\n"
                    + "- 증거: (예) 중간에 기준이 바뀌거나 시간 배분이 일정하지 않았어요.\n"
                    + "- 다음 한 줄: “오늘은 컷 편집 20분만”처럼 범위를 더 줄여볼까요?\n"
                    + "\n(분석 대상: " + title + ")";
            case RETRO -> "[회고 정리 · 임시 결과]\n"
                    + "- 잘한 점: (예) 꾸준히 시도했고 기록이 남아 있어요.\n"
                    + "- 아쉬운 점: (예) 범위를 줄이지 못해 피로가 누적됐을 수 있어요.\n"
                    + "- 다음 행동: (예) 다음엔 ‘결론 1줄’을 먼저 쓰고 본문을 채워보세요.\n"
                    + "\n(분석 대상: " + title + ")";
            case RETRY -> "[재도전 방향 · 임시 결과]\n"
                    + "- 다음 목표: (예) 7일 동안 하루 10분 루틴으로 축소해요.\n"
                    + "- 체크 포인트: (예) 3일째에 한 번만 개선하고 나머지는 유지!\n"
                    + "- 실패 대비: (예) 못 한 날은 ‘0분’ 대신 ‘1분’로라도 연결하기.\n"
                    + "\n(분석 대상: " + title + ")";
        };
    }
}
