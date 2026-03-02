package com.creati.ui.main;

import com.creati.dao.AiAnalysisDao;
import com.creati.dao.AiAnalysisDao.AiAnalysisRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AiAnalysisStore 의 DB 구현체.
 * AiAnalysisStore(인메모리) 와 동일한 public API를 제공해서
 * AiAnalysisService / Services 코드를 최소로 바꿀 수 있도록 설계.
 *
 * DB: ai_analysis 테이블
 *   a_id        VARCHAR(50) PK
 *   l_id        BIGINT      FK → log.l_id
 *   a_type      VARCHAR(20) (CAUSE | RETRO | RETRY)
 *   a_title     VARCHAR(255)
 *   a_content   TEXT
 *   a_created_at DATETIME   DEFAULT CURRENT_TIMESTAMP
 */
public class AiAnalysisRepositoryDb {

    private final AiAnalysisDao dao;

    public AiAnalysisRepositoryDb(AiAnalysisDao dao) {
        this.dao = dao;
    }

    // ─────────────────────────────────────────────
    // 목록 조회
    // ─────────────────────────────────────────────
    public List<AiAnalysisRecord> listByLogId(String logId) {
        if (logId == null || logId.isBlank()) return Collections.emptyList();

        long lId;
        try {
            lId = Long.parseLong(logId);
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }

        List<AiAnalysisRow> rows = dao.findByLogId(lId);
        List<AiAnalysisRecord> out = new ArrayList<>();
        for (AiAnalysisRow r : rows) {
            AiAnalysisRecord.Type type = parseType(r.aType);
            if (type == null) continue;
            out.add(new AiAnalysisRecord(
                    r.aId,
                    String.valueOf(r.lId),
                    type,
                    r.aTitle != null ? r.aTitle : "",
                    r.createdAt,
                    r.aContent != null ? r.aContent : ""
            ));
        }
        return out;
    }

    // ─────────────────────────────────────────────
    // 존재 여부
    // ─────────────────────────────────────────────
    public boolean hasType(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || logId.isBlank() || type == null) return false;
        long lId;
        try {
            lId = Long.parseLong(logId);
        } catch (NumberFormatException e) {
            return false;
        }
        return dao.existsByLogIdAndType(lId, type.name());
    }

    public boolean isAnalyzed(String logId) {
        if (logId == null || logId.isBlank()) return false;
        for (AiAnalysisRecord.Type t : AiAnalysisRecord.Type.values()) {
            if (!hasType(logId, t)) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // 저장
    // ─────────────────────────────────────────────
    public void add(AiAnalysisRecord r) {
        long lId;
        try {
            lId = Long.parseLong(r.logId);
        } catch (NumberFormatException e) {
            System.err.println("[AiAnalysisRepositoryDb] logId가 숫자가 아닙니다: " + r.logId);
            return;
        }
        boolean ok = dao.insert(r.id, lId, r.type.name(), r.title, r.content);
        if (!ok) {
            System.err.println("[AiAnalysisRepositoryDb] DB 저장 실패: " + r.id);
        }
    }

    // ─────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────
    private AiAnalysisRecord.Type parseType(String name) {
        if (name == null) return null;
        try {
            return AiAnalysisRecord.Type.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}