package com.creati.ui.main;

import java.time.LocalDateTime;
import java.util.*;

// DB(TODO): Replace in-memory social store with DB repository.

public final class SocialStore {

    private SocialStore() {}

    public enum ReactionType {
        EMPATHY("공감해요"),
        CHEER("힘내요"),
        PRAISE("잘했어요"),
        COMFORT("위로해요"),
        RETRY("다시 도전!");

        public final String label;
        ReactionType(String label) { this.label = label; }
    }

    public static final class Comment {
        public final String author;
        public final String text;
        public final LocalDateTime createdAt;

        public Comment(String author, String text, LocalDateTime createdAt) {
            this.author = author;
            this.text = text;
            this.createdAt = createdAt;
        }
    }

    private static final Map<String, Integer> views = new HashMap<>();
    private static final Map<String, EnumMap<ReactionType, Integer>> reactions = new HashMap<>();
    private static final Map<String, List<Comment>> comments = new HashMap<>();

    
    private static final Map<String, Map<String, ReactionType>> userReactions = new HashMap<>();

    static {
        seedDemo("qna_demo_1");
        seedDemo("log_demo_inprogress_positive");
        seedDemo("log_demo_done_regret");
        seedDemo("log_demo_need_improve_with_link");
    }

    private static void seedDemo(String postId) {
        views.putIfAbsent(postId, 1);

        EnumMap<ReactionType, Integer> rm =
                reactions.computeIfAbsent(postId, k -> new EnumMap<>(ReactionType.class));
        rm.putIfAbsent(ReactionType.EMPATHY, 1);

        comments.computeIfAbsent(postId, k -> new ArrayList<>());

        
        List<Comment> cs = comments.get(postId);
        if (cs.isEmpty()) {
            cs.add(new Comment("aaa", "저도 비슷한 경험 있어요. 결론 먼저 잡는 거 진짜 도움돼요!", LocalDateTime.now().minusHours(5)));
            cs.add(new Comment("bbb", "시간 블록 고정 팁 좋네요. 다음 시도 기대할게요 🙌", LocalDateTime.now().minusHours(2)));
        }
    }

    public static synchronized int addView(String postId) {
        int next = views.getOrDefault(postId, 0) + 1;
        views.put(postId, next);
        return next;
    }

    public static synchronized int getViews(String postId) {
        return views.getOrDefault(postId, 0);
    }

    
    public static synchronized void setUserReaction(String postId, String user, ReactionType newType) {
        if (postId == null || user == null || newType == null) return;

        Map<String, ReactionType> m =
                userReactions.computeIfAbsent(postId, k -> new HashMap<>());

        ReactionType old = m.get(user);
        if (old == newType) return; 

        EnumMap<ReactionType, Integer> counts =
                reactions.computeIfAbsent(postId, k -> new EnumMap<>(ReactionType.class));

        if (old != null) {
            counts.put(old, Math.max(0, counts.getOrDefault(old, 0) - 1));
        }

        counts.put(newType, counts.getOrDefault(newType, 0) + 1);
        m.put(user, newType);
    }

    public static synchronized ReactionType getUserReaction(String postId, String user) {
        Map<String, ReactionType> m = userReactions.get(postId);
        return (m == null) ? null : m.get(user);
    }

    
    public static synchronized void clearUserReaction(String postId, String user) {
        if (postId == null || user == null) return;
        Map<String, ReactionType> m = userReactions.get(postId);
        if (m == null) return;
        ReactionType old = m.remove(user);
        if (old == null) return;

        EnumMap<ReactionType, Integer> counts =
                reactions.computeIfAbsent(postId, k -> new EnumMap<>(ReactionType.class));
        counts.put(old, Math.max(0, counts.getOrDefault(old, 0) - 1));
    }

    
    public static synchronized void toggleUserReaction(String postId, String user, ReactionType type) {
        if (postId == null || user == null || type == null) return;
        ReactionType cur = getUserReaction(postId, user);
        if (cur == type) {
            clearUserReaction(postId, user);
        } else {
            setUserReaction(postId, user, type);
        }
    }

    // Backward-compat helper 기존 코드에서 addReaction(postId, type) 호출을 유지하기 위한 메서드. TODO(DB): 로그인 사용자 연동 시 "나" 대신 실제 userId/닉네임으로 교체. 
    public static synchronized void addReaction(String postId, ReactionType type) {
        setUserReaction(postId, "나", type);
    }

    
    public static synchronized int getReactionCount(String postId, ReactionType type) {
        EnumMap<ReactionType, Integer> m = reactions.get(postId);
        if (m == null || type == null) return 0;
        return m.getOrDefault(type, 0);
    }

    public static synchronized int getTotalReactions(String postId) {
        EnumMap<ReactionType, Integer> m = reactions.get(postId);
        if (m == null) return 0;
        int sum = 0;
        for (ReactionType t : ReactionType.values()) {
            sum += m.getOrDefault(t, 0);
        }
        return sum;
    }

    public static synchronized void addComment(String postId, Comment c) {
        comments.computeIfAbsent(postId, k -> new ArrayList<>()).add(c);
    }

    public static synchronized List<Comment> getComments(String postId) {
        return List.copyOf(comments.getOrDefault(postId, List.of()));
    }

    public static synchronized int getCommentCount(String postId) {
        return comments.getOrDefault(postId, List.of()).size();
    }
}