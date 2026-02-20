package com.creati.model;

public enum LogStatus {
    NEEDS_IMPROVEMENT("보완 필요"),
    IN_PROGRESS("진행중"),
    DONE("완료");

    public final String label;
    LogStatus(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
