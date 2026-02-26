package com.creati.ui.main;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// DB(TODO): Replace in-memory draft store with repository.

public final class DraftStore {

    private DraftStore() {}

    private static final List<WriteLogView.Draft> drafts = new ArrayList<>();

    public static synchronized List<WriteLogView.Draft> list() {
        List<WriteLogView.Draft> copy = new ArrayList<>(drafts);
        copy.sort(Comparator.comparing((WriteLogView.Draft d) -> d.updatedAt).reversed());
        return copy;
    }

    public static synchronized void upsert(WriteLogView.Draft d) {
        Objects.requireNonNull(d);
        if (d.id == null || d.id.isBlank()) {
            d.id = "draft_" + System.currentTimeMillis();
        }
        if (d.updatedAt == null) {
            d.updatedAt = LocalDateTime.now();
        } else {
            d.updatedAt = LocalDateTime.now();
        }

        for (int i = 0; i < drafts.size(); i++) {
            if (Objects.equals(drafts.get(i).id, d.id)) {
                drafts.set(i, d);
                return;
            }
        }
        drafts.add(d);
    }

    public static synchronized void delete(String id) {
        drafts.removeIf(d -> Objects.equals(d.id, id));
    }
}
