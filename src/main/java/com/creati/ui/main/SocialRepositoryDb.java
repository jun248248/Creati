package com.creati.ui.main;

import java.util.ArrayList;
import java.util.List;

import com.creati.dao.ReplyDao;
import com.creati.dto.ReplyDto;

public class SocialRepositoryDb implements SocialRepository {

    private final ReplyDao replyDao;

    public SocialRepositoryDb(ReplyDao replyDao) {
        this.replyDao = replyDao;
    }

    // -------------------------------
    // 댓글 저장
    // -------------------------------
    @Override
    public void addComment(String postId, SocialStore.Comment c) {
        long logId = Long.parseLong(postId);
        replyDao.insertReply(logId, c.author, c.text);
    }

    // -------------------------------
    // 댓글 조회
    // -------------------------------
    @Override
    public List<SocialStore.Comment> getComments(String postId) {
        long logId = Long.parseLong(postId);

        List<ReplyDto> dtos = replyDao.getRepliesByLogId(logId);
        List<SocialStore.Comment> result = new ArrayList<>();

        for (ReplyDto dto : dtos) {
            result.add(
                new SocialStore.Comment(
                    dto.getNickname(),
                    dto.getContent(),
                    dto.getCreatedAt()
                )
            );
        }

        return result;
    }

    @Override
    public int getCommentCount(String postId) {
        return getComments(postId).size();
    }

    // ---------------------------------
    // 아직 DB 연결 안 한 기능들
    // ---------------------------------
    @Override
    public int addView(String postId) { return 0; }

    @Override
    public int getViews(String postId) { return 0; }

    @Override
    public void toggleUserReaction(String postId, String userId, SocialStore.ReactionType type) {}

    @Override
    public SocialStore.ReactionType getUserReaction(String postId, String userId) { return null; }

    @Override
    public int getReactionCount(String postId, SocialStore.ReactionType type) { return 0; }

    @Override
    public int getTotalReactions(String postId) { return 0; }
}