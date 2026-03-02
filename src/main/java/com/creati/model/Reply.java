package com.creati.model;

import java.time.LocalDateTime;

import com.creati.dto.ReplyDto;

/**
 * Reply - 댓글 도메인 모델
 * ReplyDto(DB 전용)와 분리하여 UI 레이어에서 사용
 */
public class Reply {

    public final long rId;
    public final long lId;
    public final String userId;
    public final String nickname;
    public final String content;
    public final LocalDateTime createdAt;

    public Reply(long rId, long lId, String userId, String nickname, String content, LocalDateTime createdAt) {
        this.rId = rId;
        this.lId = lId;
        this.userId = userId;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
    }

    /** ReplyDto → Reply 변환 */
    public static Reply from(ReplyDto dto) {
        return new Reply(
            dto.getrId(),
            dto.getlId(),
            dto.getUserId(),
            dto.getNickname(),
            dto.getContent(),
            dto.getCreatedAt()
        );
    }
}