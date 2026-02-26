package com.creati.ui.main;

import java.util.List;

import com.creati.model.LogPost;
public interface LogRepository {
    List<LogPost> list();
    LogPost getById(String id);
    void upsert(LogPost post);
}
