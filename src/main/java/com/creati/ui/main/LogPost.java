package com.creati.ui.main;

import com.creati.model.LogStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LogPost
 * - 상세 화면(LogDetailView)에서 표시할 데이터 구조
 *
 * TODO(DB)
 * - DB 연동 시 id(PK), author, updatedAt 등을 추가 구성
 * - ChallengeView / WriteLogView에서 DB 조회 결과를 이 DTO로 매핑해 사용
 */
public class LogPost {

    public final String id;             // TODO (DB) PK
    public final String field;          // 분야
    public final String subCategory;    // 카테고리
    public final LogStatus status;      // 현재 상태

    public final String title;
    public final LocalDate createdAt;
    public final boolean isPublic;

    public final String whatIDid;
    public final String feeling;
    public final String difficulty;
    public final String learning;
    public final String retryPlan;
    public final String link;

    // v2 fields (WriteLogView 2026.02 flow) ---
    public final String goalText;                 // 기대했던 점
    public final String mood;                     // 진행 느낌(결과 인식)
    public final List<String> goodPoints;         // 잘 된 부분(체크)
    public final String goodOther;                // 잘 된 부분 기타
    public final String painPoint;                // 아쉬웠던 부분
    public final List<String> influenceFactors;   // 영향 요인(체크)
    public final String influenceOther;           // 영향 요인 기타
    public final String processText;              // 행동 과정
    public final String planGapLevel;             // 계획 차이(선택)
    public final String planGapDetail;            // 차이 내용(서술)
    public final String learningText;             // 회고
    public final List<String> nextAdjustPoints;   // 다음 조정 포인트(체크)
    public final String nextAdjustOther;          // 다음 조정 기타
    public final String nextPlan;                 // 다음 시도 계획(선택)
    public final String retryCondition;           // 재시도 조건(서술)
    public final String linkUrl;                  // 업로드 링크
    public final String linkPoint;                // 링크 확인 포인트

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
        this(
                id,
                field,
                subCategory,
                status,
                title,
                createdAt,
                isPublic,
                // v2 (unknown in legacy calls)
                null, null, null, null,
                null, null, null,
                whatIDid, null, null,
                learning, null, null,
                null, retryPlan,
                link, null
        );
    }

    /**
     * v2 생성자: WriteLogView 최신 질문형 입력 구조에 맞춘 상세 데이터.
     *
     * NOTE
     * - 기존 필드(whatIDid/feeling/difficulty/learning/retryPlan/link)도 함께 채워
     *   예전 UI가 붙어있어도 최소 정보는 표시되도록 합니다.
     */
    public LogPost(
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

        // legacy aliases
        this.whatIDid = (processText == null) ? "" : processText;
        this.feeling = (mood == null) ? "" : mood;
        this.difficulty = (painPoint == null) ? "" : painPoint;
        this.learning = (learningText == null) ? "" : learningText;
        this.retryPlan = (retryCondition == null) ? "" : retryCondition;
        this.link = (linkUrl == null) ? "" : linkUrl;
    }

    /**
     * 호환 생성자: 기존(분야=category)만 쓰던 코드 호환
     */
    public LogPost(
            String field,
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
                null,
                field,
                "일상 / 브이로그",
                LogStatus.IN_PROGRESS,
                title,
                createdAt,
                isPublic,
                whatIDid,
                feeling,
                difficulty,
                learning,
                retryPlan,
                link
        );
    }

    private static List<String> safeList(List<String> src) {
        if (src == null || src.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(src));
    }
}
