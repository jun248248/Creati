package com.creati.model;

import com.creati.dao.AiAnalysisDao;
import com.creati.dao.AiAnalysisDao.AiAnalysisRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 분석 저장소.
 * 기존 인메모리(HashMap) 구현을 DB(ai_analysis 테이블) 구현으로 교체.
 * AiAnalysisService / Services 의 생성자 시그니처는 변경하지 않음.
 */
public class AiAnalysisStore {

    private final AiAnalysisDao dao;

    public AiAnalysisStore() {
        this.dao = new AiAnalysisDao();
    }

    // ─────────────────────────────────────────────
    // 목록 조회
    // ─────────────────────────────────────────────
    public synchronized List<AiAnalysisRecord> listByLogId(String logId) {
        if (logId == null || logId.isBlank()) return Collections.emptyList();
        long lId = parseLong(logId);
        if (lId < 0) return Collections.emptyList();

        List<AiAnalysisRow> rows = dao.findByLogId(lId);
        List<AiAnalysisRecord> out = new ArrayList<>();
        for (AiAnalysisRow r : rows) {
            AiAnalysisRecord.Type type = parseType(r.aType);
            if (type == null) continue;
            out.add(new AiAnalysisRecord(
                    r.aId,
                    String.valueOf(r.lId),
                    type,
                    r.aTitle  != null ? r.aTitle  : "",
                    r.createdAt,
                    r.aContent != null ? r.aContent : ""
            ));
        }
        return out;
    }

    // ─────────────────────────────────────────────
    // 저장
    // ─────────────────────────────────────────────
    public synchronized void add(AiAnalysisRecord r) {
        long lId = parseLong(r.logId);
        if (lId < 0) {
            System.err.println("[AiAnalysisStore] logId 숫자 변환 실패: " + r.logId);
            return;
        }
        boolean ok = dao.insert(r.id, lId, r.type.name(), r.title, r.content);
        if (!ok) {
            System.err.println("[AiAnalysisStore] DB INSERT 실패: id=" + r.id);
        }
    }

    // ─────────────────────────────────────────────
    // 존재 여부
    // ─────────────────────────────────────────────
    public synchronized boolean hasType(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || logId.isBlank() || type == null) return false;
        long lId = parseLong(logId);
        if (lId < 0) return false;
        return dao.existsByLogIdAndType(lId, type.name());
    }

    public synchronized boolean isAnalyzed(String logId) {
        if (logId == null || logId.isBlank()) return false;
        for (AiAnalysisRecord.Type t : AiAnalysisRecord.Type.values()) {
            if (!hasType(logId, t)) return false;
        }
        return true;
    }

    public synchronized void clearAll(String logId) {
        // 필요 시 DELETE 구현
    }

    // ─────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────
    private long parseLong(String s) {
        if (s == null) return -1;
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private AiAnalysisRecord.Type parseType(String name) {
        if (name == null) return null;
        try { return AiAnalysisRecord.Type.valueOf(name.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}