package com.creati.ui.main;

import java.util.List;
import java.util.Objects;

import com.creati.model.QnaPost;
public class QnaService {

    private final QnaRepository repo;

    public QnaService(QnaRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public List<QnaPost> list() {
        return repo.list();
    }

    public void add(QnaPost post) {
        repo.add(post);
    }
}
