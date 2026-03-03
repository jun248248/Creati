package com.creati.service;

import java.util.List;
import java.util.Objects;

import com.creati.model.DraftRepository;
import com.creati.ui.main.WriteLogView;
import com.creati.ui.main.WriteLogView.Draft;

public class DraftService {

    private final DraftRepository repo;

    public DraftService(DraftRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public List<WriteLogView.Draft> list() {
        return repo.list();
    }

    public void upsert(WriteLogView.Draft d) {
        repo.upsert(d);
    }

    public void delete(String id) {
        repo.delete(id);
    }
}
