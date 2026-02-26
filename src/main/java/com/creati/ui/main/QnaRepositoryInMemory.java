package com.creati.ui.main;

import java.util.List;

import com.creati.model.QnaPost;
// DB(TODO): Replace with DB-backed repository.

public class QnaRepositoryInMemory implements QnaRepository {

    @Override
    public List<QnaPost> list() {
        return QnaStore.list();
    }

    @Override
    public void add(QnaPost post) {
        QnaStore.add(post);
    }
}
