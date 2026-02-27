package com.creati.ui.main;

import java.time.LocalDate;
import java.util.Objects;

import com.creati.model.LogPost;

/**
 * [COLLAB] 질문 작성(QuestionWriteView)의 입력 처리/검증/저장 흐름을 View 밖으로 분리.
 * - View: 레이아웃 + 이벤트 트리거 + 메시지 표시만 담당
 * - Controller: 입력 검증/모델 생성/Service 호출 담당
 */
public class QuestionWriteController {

    private final LogService logs;

    public QuestionWriteController(LogService logs) {
        this.logs = Objects.requireNonNull(logs);
    }

    public LogPost submit(QuestionWriteRequest req) {
        Objects.requireNonNull(req);

        String title = safe(req.title);
        String content = safe(req.content);

        if (title.isBlank()) {
            throw new ValidationException(ValidationException.Field.TITLE, "제목을 입력해 주세요.");
        }
        if (content.isBlank()) {
            throw new ValidationException(ValidationException.Field.CONTENT, "질문 내용을 입력해 주세요.");
        }

        String id = "qna_" + System.currentTimeMillis(); // DB(TODO): PK
        String field = safe(req.field);
        if (field.isBlank()) field = "기타";

        String category = safe(req.category);
        if (category.isBlank()) category = "기타";

        String link = safe(req.link);
        LocalDate createdAt = (req.createdAt == null) ? LocalDate.now() : req.createdAt;

        LogPost post = LogPost.newQna(
                id,
                field,
                category,
                title,
                createdAt,
                content,
                link
        );

        logs.save(post); // TODO(DB)
        return post;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    /** View → Controller 입력 DTO */
    public static class QuestionWriteRequest {
        public final String title;
        public final String content;
        public final String field;
        public final String category;
        public final String link;
        public final LocalDate createdAt;

        public QuestionWriteRequest(String title, String content, String field, String category, String link, LocalDate createdAt) {
            this.title = title;
            this.content = content;
            this.field = field;
            this.category = category;
            this.link = link;
            this.createdAt = createdAt;
        }
    }
}
