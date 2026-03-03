package com.creati.model;

import java.util.List;

import com.creati.ui.main.LogRepository;

public class LogRepositoryInMemory implements LogRepository {

    @Override
    public List<LogPost> list() {
        return LogStore.list();
    }

    @Override
    public LogPost getById(String id) {
        return LogStore.getById(id);
    }

    @Override
    public void upsert(LogPost post) {
        LogStore.upsert(post);
    }
}
