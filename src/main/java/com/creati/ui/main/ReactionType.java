package com.creati.ui.main;

public enum ReactionType {
    EMPATHY(1, "공감해요"),
    CHEER(2, "힘내요"),
    PRAISE(3, "잘했어요"),
    COMFORT(4, "위로해요"),
    RETRY(5, "다시 도전!");

    private final int id;
    private final String label;

    ReactionType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ReactionType fromId(int id) {
        for (ReactionType t : values()) {
            if (t.id == id) return t;
        }
        return null;
    }
}