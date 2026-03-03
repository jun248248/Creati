package com.creati.model;

import com.creati.model.LogStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogPost {

    // DB(TODO): 글 유형 구분(성장로그/질문하기)
    public static final String TYPE_LOG = "LOG";
    public static final String TYPE_QNA = "QNA";

    public final String type;

    public final String id;          // DB(TODO): PK
    public final String field;       
    public final String subCategory; 
    public final LogStatus status;   

    public final String title;
    public final LocalDate createdAt;
    public final boolean isPublic;   

    
    public final String whatIDid;
    public final String feeling;
    public final String difficulty;
    public final String learning;
    public final String retryPlan;
    public final String link;

    
    public final String goalText;
    public final String mood;
    public final List<String> goodPoints;
    public final String goodOther;
    public final String painPoint;
    public final List<String> influenceFactors;
    public final String influenceOther;
    public final String processText;
    public final String planGapLevel;
    public final String planGapDetail;
    public final String learningText;
    public final List<String> nextAdjustPoints;
    public final String nextAdjustOther;
    public final String nextPlan;
    public final String retryCondition;
    public final String linkUrl;
    public final String linkPoint;
    public String authorId;   // ← 추가: 작성자 u_id
    public String authorNick; // ← 추가: 작성자 닉네임
    
    
    
    public LogPost(
            String type,
            String id,
            String field,
            String subCategory,
            LogStatus status,
            String title,
            LocalDate createdAt,
            boolean isPublic,
            String whatIDid,
            String feeling,
            String difficulty,
            String learning,
            String retryPlan,
            String link
    ) {
        this(
                type,
                id,
                field,
                subCategory,
                status,
                title,
                createdAt,
                isPublic,
                null, null, null, null,
                null, null, null,
                whatIDid, null, null,
                learning, null, null,
                null, retryPlan,
                link, null
        );
    }

    
    public LogPost(
            String id,
            String field,
            String subCategory,
            LogStatus status,
            String title,
            LocalDate createdAt,
            boolean isPublic,
            String whatIDid,
            String feeling,
            String difficulty,
            String learning,
            String retryPlan,
            String link
    ) {
        this(TYPE_LOG, id, field, subCategory, status, title, createdAt, isPublic,
                whatIDid, feeling, difficulty, learning, retryPlan, link);
    }

    
    public static LogPost newQna(
            String id,
            String field,
            String subCategory,
            String title,
            LocalDate createdAt,
            String questionText,
            String linkUrl
    ) {
        return new LogPost(
                TYPE_QNA,
                id,
                field,
                subCategory,
                LogStatus.IN_PROGRESS,
                title,
                createdAt,
                true,
                
                null, null, null, null,
                null, null, null,
                questionText, null, null,
                null, null, null,
                null, null,
                linkUrl, null
        );
    }

    
    
    
    public LogPost(
            String type,
            String id,
            String field,
            String subCategory,
            LogStatus status,
            String title,
            LocalDate createdAt,
            boolean isPublic,
            String goalText,
            String mood,
            List<String> goodPoints,
            String goodOther,
            String painPoint,
            List<String> influenceFactors,
            String influenceOther,
            String processText,
            String planGapLevel,
            String planGapDetail,
            String learningText,
            List<String> nextAdjustPoints,
            String nextAdjustOther,
            String nextPlan,
            String retryCondition,
            String linkUrl,
            String linkPoint
    ) {
        this.type = (type == null || type.isBlank()) ? TYPE_LOG : type;

        this.id = id;
        this.field = field;
        this.subCategory = subCategory;
        this.status = status;
        this.title = title;
        this.createdAt = createdAt;
        this.isPublic = isPublic;

        this.goalText = goalText;
        this.mood = mood;
        this.goodPoints = safeList(goodPoints);
        this.goodOther = goodOther;
        this.painPoint = painPoint;
        this.influenceFactors = safeList(influenceFactors);
        this.influenceOther = influenceOther;
        this.processText = processText;
        this.planGapLevel = planGapLevel;
        this.planGapDetail = planGapDetail;
        this.learningText = learningText;
        this.nextAdjustPoints = safeList(nextAdjustPoints);
        this.nextAdjustOther = nextAdjustOther;
        this.nextPlan = nextPlan;
        this.retryCondition = retryCondition;
        this.linkUrl = linkUrl;
        this.linkPoint = linkPoint;

        
        this.whatIDid = (processText == null) ? "" : processText;
        this.feeling = (mood == null) ? "" : mood;
        this.difficulty = (painPoint == null) ? "" : painPoint;
        this.learning = (learningText == null) ? "" : learningText;
        this.retryPlan = (retryCondition == null) ? "" : retryCondition;
        this.link = (linkUrl == null) ? "" : linkUrl;
    }

    private static List<String> safeList(List<String> src) {
        if (src == null || src.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(src));
    }
}
