package com.creati.ui.main;

import java.util.List;
import java.util.Objects;

import com.creati.model.LogPost;

public class LogService {

    private final LogRepository repo;

    public LogService(LogRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public List<LogPost> list() {
        return repo.list();
    }

    public LogPost getById(String id) {
        return repo.getById(id);
    }

    public void save(LogPost post) {
        repo.upsert(post);
    }

    
    public void upsert(LogPost post) {
        save(post);
    }
}
