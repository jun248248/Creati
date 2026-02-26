
package com.creati.model;

import java.time.LocalDate;

public class QnaPost {
    public final String id;      // DB(TODO): PK
    public final String field;
    public final String category;
    public final String title;
    public final String content;
    public final String linkUrl;
    public final LocalDate createdAt;

    public QnaPost(String id, String field, String category,
                   String title, String content, String linkUrl,
                   LocalDate createdAt) {
        this.id = id;
        this.field = field;
        this.category = category;
        this.title = title;
        this.content = content;
        this.linkUrl = linkUrl;
        this.createdAt = createdAt;
    }
}
