package com.creati.ui.main;

import java.util.List;

public class DraftRepositoryInMemory implements DraftRepository {

    @Override
    public List<WriteLogView.Draft> list() {
        return DraftStore.list();
    }

    @Override
    public void upsert(WriteLogView.Draft d) {
        DraftStore.upsert(d);
    }

    @Override
    public void delete(String id) {
        DraftStore.delete(id);
    }
}
