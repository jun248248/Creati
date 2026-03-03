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

    private Long parseId(String postId) {
        try {
            return Long.parseLong(postId);
        } catch (NumberFormatException e) {
            return null; // 더미 데이터면 null 반환
        }
    }

    @Override
    public void addComment(String postId, SocialStore.Comment c) {
        Long logId = parseId(postId);
        if (logId == null) return;
        replyDao.insertReply(logId, c.author, c.text);
    }

    @Override
    public List<SocialStore.Comment> getComments(String postId) {
        Long logId = parseId(postId);
        if (logId == null) return new ArrayList<>();

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
        Long logId = parseId(postId);
        if (logId == null) return 0;
        logDao.increaseView(logId);
        return logDao.getViewCount(logId);
    }

    @Override
    public int getViews(String postId) {
        Long logId = parseId(postId);
        if (logId == null) return 0;
        return logDao.getViewCount(logId);
    }

    @Override
    public void toggleUserReaction(String postId, String userId, SocialStore.ReactionType type) {
        Long logId = parseId(postId);
        if (logId == null) return;
        long rtId = type.ordinal() + 1;

        Long existing = reactionDao.getUserReaction(logId, userId);

        if (existing == null) {
            reactionDao.insertReaction(logId, rtId, userId);
        } else if (existing == rtId) {
            reactionDao.deleteReaction(logId, userId);
        } else {
            reactionDao.updateReaction(logId, rtId, userId);
        }
    }

    @Override
    public SocialStore.ReactionType getUserReaction(String postId, String userId) {
        Long logId = parseId(postId);
        if (logId == null) return null;

        Long rtId = reactionDao.getUserReaction(logId, userId);
        if (rtId == null) return null;

        return SocialStore.ReactionType.values()[(int)(rtId - 1)];
    }

    @Override
    public int getReactionCount(String postId, SocialStore.ReactionType type) {
        Long logId = parseId(postId);
        if (logId == null) return 0;
        return reactionDao.countByReaction(logId, type.ordinal() + 1);
    }

    @Override
    public int getTotalReactions(String postId) {
        Long logId = parseId(postId);
        if (logId == null) return 0;
        return reactionDao.countTotal(logId);
    }
}