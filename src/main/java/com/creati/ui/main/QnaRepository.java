package com.creati.ui.main;

import java.util.List;

import com.creati.model.QnaPost;
public interface QnaRepository {
    List<QnaPost> list();
    void add(QnaPost post);
}
