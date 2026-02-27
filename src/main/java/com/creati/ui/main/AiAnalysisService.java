package com.creati.ui.main;

import com.creati.model.LogPost;
import com.creati.service.GptAnalysisService;
import com.creati.dao.AiAnalysisDao; // Dao 임포트 확인

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AiAnalysisService {

    // [수정] 메모리 Store 대신 실제 DB Dao를 사용합니다.
    private final AiAnalysisDao aiDao = new AiAnalysisDao();
    private final GptAnalysisService gptService = new GptAnalysisService();
    
    // [수정] 인자 없는 기본 생성자로 변경하여 Services.java와의 호환성을 맞춥니다.
    public AiAnalysisService() {
    }

    /**
     * 특정 로그가 완전히 분석되었는지 확인 (DB 조회)
     */
    public boolean isAnalyzed(String logId) {
        if (logId == null) return false;
        // DB에서 해당 로그의 분석 레코드가 3개 이상이면 완료로 간주
        return aiDao.findByLogId(logId).size() >= 3;
    }

    /**
     * 특정 유형의 분석이 이미 DB에 존재하는지 확인
     */
    public boolean isTypeAnalyzed(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || type == null) return false;
        return aiDao.findByLogId(logId).stream()
                    .anyMatch(r -> r.type == type);
    }

    /**
     * DB에서 특정 로그의 분석 기록 리스트를 가져옵니다.
     */
    public List<AiAnalysisRecord> listRecords(String logId) {
        if (logId == null || logId.isBlank()) return List.of();
        return aiDao.findByLogId(logId);
    }

    public LogPost findMostRecentLog() {
        return Services.LOGS.list().stream()
            .filter(p -> p != null && "LOG".equals(p.type))
            .max(Comparator.comparing(p -> p.createdAt))
            .orElse(null);
    }

    /**
     * 분석 결과를 실제 DB에 저장합니다.
     */
    public AiAnalysisRecord save(String logId, AiAnalysisRecord.Type type, String content) {
        try {
            // [중요] DB의 BIGINT 타입에 맞게 숫자만 추출하여 변환
            long l_id = Long.parseLong(logId.replaceAll("[^0-9]", ""));
            
            // Dao를 호출하여 실제 INSERT 실행
            aiDao.insertAnalysis(l_id, type.name(), "에티의 " + type.label, content);
            
        } catch (Exception e) {
            System.err.println("DB 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        // UI 갱신을 위해 객체 반환
        return new AiAnalysisRecord(
            UUID.randomUUID().toString(), logId, type, "에티의 " + type.label, LocalDate.now(), content
        );
    }

    /**
     * 실제 GPT API를 호출하여 분석 결과를 받아옵니다.
     */
    public String requestAiAnalysis(String logId, AiAnalysisRecord.Type type) throws Exception {
        LogPost log = Services.LOGS.getById(logId);
        if (log == null) return "로그를 찾을 수 없습니다.";

        String prompt = String.format(
            "너는 크리에이터 컨설턴트 에티야. 제목: [%s], 내용: [%s]. [%s] 관점에서 분석해줘." + "답변이 도중에 끊기지 않도록 문장을 완결해줘.",
            log.title, log.whatIDid, type.label
        );
        return gptService.analyzeWithPrompt(prompt);
    }

    // 기존 미리보기 로직 (필요 시 유지)
    public AiAnalysisRecord preview(String logId, AiAnalysisRecord.Type type) {
        return new AiAnalysisRecord(
                "preview_" + UUID.randomUUID(),
                logId, type, "미리보기", LocalDate.now(), "분석 중..."
        );
    }
}