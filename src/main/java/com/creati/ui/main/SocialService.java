package com.creati.ui.main;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.creati.model.User;

/**
 * SocialService - UI에서 직접 Store를 만지지 않게 하는 "접점"
 *
 * DB(TODO): SocialRepository 구현만 교체하면 UI는 그대로 유지
 */
public class SocialService {

    private final SocialRepository repo;

    public SocialService(SocialRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public int addView(String postId) {
        return repo.addView(postId);
    }

    public int getViews(String postId) {
        return repo.getViews(postId);
    }

    /**
     * 로그인 사용자 연동 전 임시 userId.
     * DB(TODO): 로그인/세션 붙이면 실제 userId로 교체
     */
    private String currentUserId() {
        User user = AppState.get().getCurrentUser();
        if (user == null) return null;   // 또는 예외 처리
        return user.getId();
    }

    public void toggleReaction(String postId, SocialStore.ReactionType type) {
        repo.toggleUserReaction(postId, currentUserId(), type);
    }

    public SocialStore.ReactionType getMyReaction(String postId) {
        return repo.getUserReaction(postId, currentUserId());
    }

    public int getReactionCount(String postId, SocialStore.ReactionType type) {
        return repo.getReactionCount(postId, type);
    }

    public int getTotalReactions(String postId) {
        return repo.getTotalReactions(postId);
    }

    public void addComment(String postId, String text) {
        if (text == null || text.isBlank()) return;
        repo.addComment(postId, new SocialStore.Comment(currentUserId(), text.trim(), LocalDateTime.now()));
    }

    public List<SocialStore.Comment> listComments(String postId) {
        return repo.getComments(postId);
    }

    public int getCommentCount(String postId) {
        return repo.getCommentCount(postId);
    }
}
