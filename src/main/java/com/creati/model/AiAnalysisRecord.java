package com.creati.model;

import java.time.LocalDate;
import java.util.Objects;

// DB(TODO): Map to DTO/Entity for persistence.

public class AiAnalysisRecord {

    public enum Type {
        CAUSE("원인 분석"),
        RETRO("회고 정리"),
        RETRY("재도전 방향");

        public final String label;
        Type(String label) {
            this.label = label;
        }
    }

    public final String id;
    public final String logId;
    public final Type type;
    public final String title;
    public final LocalDate createdAt;
    public final String content;

    public AiAnalysisRecord(String id, String logId, Type type, String title, LocalDate createdAt, String content) {
        this.id = Objects.requireNonNull(id);
        this.logId = Objects.requireNonNull(logId);
        this.type = Objects.requireNonNull(type);
        this.title = Objects.requireNonNull(title);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.content = Objects.requireNonNull(content);
    }
}
