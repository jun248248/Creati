package com.creati.ui.main;

import java.util.List;

/**
 * In-memory implementation (화면 테스트용)
 *
 * DB(TODO): DB 저장소 구현체로 교체 (예: SocialRepositoryJdbc / SocialRepositoryJpa 등)
 */
public class SocialRepositoryInMemory implements SocialRepository {

    @Override
    public int addView(String postId) {
        return SocialStore.addView(postId);
    }

    @Override
    public int getViews(String postId) {
        return SocialStore.getViews(postId);
    }

    @Override
    public void toggleUserReaction(String postId, String userId, SocialStore.ReactionType type) {
        SocialStore.toggleUserReaction(postId, userId, type);
    }

    @Override
    public SocialStore.ReactionType getUserReaction(String postId, String userId) {
        return SocialStore.getUserReaction(postId, userId);
    }

    @Override
    public int getReactionCount(String postId, SocialStore.ReactionType type) {
        return SocialStore.getReactionCount(postId, type);
    }

    @Override
    public int getTotalReactions(String postId) {
        return SocialStore.getTotalReactions(postId);
    }

    @Override
    public void addComment(String postId, SocialStore.Comment c) {
        SocialStore.addComment(postId, c);
    }

    @Override
    public List<SocialStore.Comment> getComments(String postId) {
        return SocialStore.getComments(postId);
    }

    @Override
    public int getCommentCount(String postId) {
        return SocialStore.getCommentCount(postId);
    }
}
