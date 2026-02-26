
package com.creati.ui.main;

import java.util.*;
import java.time.LocalDate;

import com.creati.model.QnaPost;
public class QnaStore {
    private static final List<QnaPost> POSTS = new ArrayList<>();

    static {
        POSTS.add(new QnaPost(
            "qna_demo_1",
            "영상",
            "편집",
            "쇼츠 자막 템포 질문",
            "쇼츠 편집 시 자막 속도는 어떻게 맞추는 게 좋을까요?",
            null,
            LocalDate.now()
        ));
    }

    public static List<QnaPost> list() {
        return Collections.unmodifiableList(POSTS);
    }

    public static void add(QnaPost post) {
        POSTS.add(post); // DB(TODO): insert
    }
}
