package com.creati.ui.main;

import java.util.*;
// 제거 요망 클래스(임시 저장소)
// DB(TODO): Replace in-memory store with repository.

public class AiAnalysisStore {

    private final Map<String, List<AiAnalysisRecord>> byLogId = new HashMap<>();

    public synchronized List<AiAnalysisRecord> listByLogId(String logId) {
        List<AiAnalysisRecord> list = byLogId.get(logId);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public synchronized void add(AiAnalysisRecord r) {
        byLogId.computeIfAbsent(r.logId, k -> new ArrayList<>()).add(r);
    }

    
    public synchronized boolean hasType(String logId, AiAnalysisRecord.Type type) {
        if (logId == null || logId.isBlank() || type == null) return false;
        List<AiAnalysisRecord> list = byLogId.get(logId);
        if (list == null || list.isEmpty()) return false;
        for (AiAnalysisRecord r : list) {
            if (r != null && r.type == type) return true;
        }
        return false;
    }

    
    public synchronized boolean isAnalyzed(String logId) {
        if (logId == null || logId.isBlank()) return false;
        for (AiAnalysisRecord.Type t : AiAnalysisRecord.Type.values()) {
            if (!hasType(logId, t)) return false;
        }
        return true;
    }

    public synchronized void clearAll(String logId) {
        byLogId.remove(logId);
    }
}
