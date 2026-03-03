package com.creati.model;

import java.util.List;

import com.creati.ui.main.WriteLogView;
import com.creati.ui.main.WriteLogView.Draft;

public interface DraftRepository {
    List<WriteLogView.Draft> list();
    void upsert(WriteLogView.Draft d);
    void delete(String id);
}
