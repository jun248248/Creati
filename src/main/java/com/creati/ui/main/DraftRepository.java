package com.creati.ui.main;

import java.util.List;

public interface DraftRepository {
    List<WriteLogView.Draft> list();
    void upsert(WriteLogView.Draft d);
    void delete(String id);
}
