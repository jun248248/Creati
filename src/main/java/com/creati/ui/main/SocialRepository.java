package com.creati.ui.main;

import java.util.List;

/**
 * SocialRepository - 조회수/리액션/댓글 등 "커뮤니티 상호작용" 저장소 추상화
 */
public interface SocialRepository {

    int addView(String postId);
    int getViews(String postId);

    void toggleUserReaction(String postId, String userId, SocialStore.ReactionType type);
    SocialStore.ReactionType getUserReaction(String postId, String userId);

    int getReactionCount(String postId, SocialStore.ReactionType type);
    int getTotalReactions(String postId);

    void addComment(String postId, SocialStore.Comment c);
    List<SocialStore.Comment> getComments(String postId);
    int getCommentCount(String postId);
}
