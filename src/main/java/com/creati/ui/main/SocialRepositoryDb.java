package com.creati.ui.main;

import java.util.ArrayList;
import java.util.List;

import com.creati.dao.LogDao;
import com.creati.dao.ReactionDao;
import com.creati.dao.ReplyDao;
import com.creati.dto.ReplyDto;

public class SocialRepositoryDb implements SocialRepository {

    private final ReplyDao replyDao;
    private final ReactionDao reactionDao;
    private final LogDao logDao;

    public SocialRepositoryDb(ReplyDao replyDao, ReactionDao reactionDao, LogDao logDao) {
        this.replyDao = replyDao;
        this.reactionDao = reactionDao;
        this.logDao = logDao;
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

    @Override
    public int addView(String postId) {

        long logId = Long.parseLong(postId);

        logDao.increaseView(logId);

        return logDao.getViewCount(logId);
    }

    @Override
    public int getViews(String postId) {

        long logId = Long.parseLong(postId);

        return logDao.getViewCount(logId);
    }

    @Override
    public void toggleUserReaction(String postId, String userId, SocialStore.ReactionType type) {

        long logId = Long.parseLong(postId);
        long rtId = type.ordinal() + 1;

        Long existing = reactionDao.getUserReaction(logId, userId);

        if (existing == null) {
            reactionDao.insertReaction(logId, rtId, userId);
        } else if (existing == rtId) {
            // 같은거 다시 누르면 취소
            reactionDao.deleteReaction(logId, userId);
        } else {
            reactionDao.updateReaction(logId, rtId, userId);
        }
    }
    
    @Override
    public SocialStore.ReactionType getUserReaction(String postId, String userId) {

        Long rtId = reactionDao.getUserReaction(Long.parseLong(postId), userId);

        if (rtId == null) return null;

        return SocialStore.ReactionType.values()[(int)(rtId - 1)];
    }

    @Override
    public int getReactionCount(String postId, SocialStore.ReactionType type) {
        return reactionDao.countByReaction(Long.parseLong(postId), type.ordinal() + 1);
    }

    @Override
    public int getTotalReactions(String postId) {
        return reactionDao.countTotal(Long.parseLong(postId));
    }
}