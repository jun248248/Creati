package com.creati.service;

import com.creati.dto.LogDto;
import java.util.List;

/**
 * 규칙 기반 로그 비교 점수 계산기 
 * DB 필드 매핑:
 *   nextAdjust     → l_reflection
 *   nextPlan       → next_plan_type
 *   retryCondition → retry_condition
 *   planGap        → l_plan_difference
 *   planGapDetail  → l_difference
 *   mood           → l_result_rating
 *   process/good/pain → l_process
 *   uploadUrl      → l_content_url
 *   urlCheckPoint  → l_goal (임시 매핑)
 */
public class LogCompareScorer {

    public static class ScoreBreakdown {
        public final int decision;      // (1) 다음 정리 쉬움
        public final int planFit;       // (2) 계획-실행 맞춤
        public final int cause;         // (3) 아쉬움 정리
        public final int tweakClarity;  // (4) 다음에 바꿀 것 뚜렷함
        public final int retryReady;    // (5) 다음 시도 준비
        public final int evidence;      // (6) 증거/확인 포인트
        public final int total;

        public ScoreBreakdown(int decision, int planFit, int cause,
                              int tweakClarity, int retryReady, int evidence) {
            this.decision     = clamp(decision);
            this.planFit      = clamp(planFit);
            this.cause        = clamp(cause);
            this.tweakClarity = clamp(tweakClarity);
            this.retryReady   = clamp(retryReady);
            this.evidence     = clamp(evidence);
            this.total = this.decision + this.planFit + this.cause
                       + this.tweakClarity + this.retryReady + this.evidence;
        }

        private static int clamp(int v) { return Math.max(0, Math.min(2, v)); }
    }

    public static ScoreBreakdown score(LogDto log) {
        if (log == null) return new ScoreBreakdown(0, 0, 0, 0, 0, 0);

        // nextAdjustPoints 리스트 → 문자열로 변환 (2개 이상이면 multi로 판단)
        List<String> adjList = log.getNextAdjustPoints();
        String nextAdjust = (adjList != null && !adjList.isEmpty())
            ? String.join("\n", adjList)   // 리스트를 줄바꿈으로 연결 → scoreTweakClarity에서 \n 감지
            : s(log.getReflection());      // fallback: l_reflection

        String nextPlan      = s(log.getNextPlanType());
        String retry         = s(log.getRetryCondition());
        String planGap       = s(log.getPlanDifference());
        String planGapDetail = s(log.getDifference());
        String rating        = s(log.getResultRating());
        String process       = s(log.getProcess());
        String uploadUrl     = s(log.getContentUrl());
        String checkPoint    = s(log.getGoal());

        return new ScoreBreakdown(
            scoreDecision(nextPlan, retry, nextAdjust),
            scorePlanFit(planGap, planGapDetail, nextAdjust),
            scoreCause(rating, process),
            scoreTweakClarity(nextAdjust),
            scoreRetryReady(nextPlan, retry),
            scoreEvidence(uploadUrl, checkPoint)
        );
    }

    // ── (1) 다음 정리 쉬움
    private static int scoreDecision(String nextPlan, String retry, String adjust) {
        if (nextPlan.isEmpty()) return 0;
        boolean isHesitant = isHesitantPlan(nextPlan);
        boolean hasRetry   = !retry.isEmpty();
        boolean hasAdjust  = !adjust.isEmpty();
        if (isHesitant && !hasRetry && !hasAdjust) return 0;
        if (hasRetry || hasAdjust) return 2;
        return 1;
    }

    // ── (2) 계획-실행 맞춤
    private static int scorePlanFit(String planGap, String detail, String adjust) {
        boolean isMismatched = planGap.contains("일부") || planGap.contains("많이");
        boolean hasDetail    = !detail.isEmpty();
        boolean hasAdjust    = !adjust.isEmpty();
        if (isMismatched && !hasDetail) return 0;
        if (hasDetail && hasAdjust) return 2;
        return 1;
    }

    // ── (3) 아쉬움 정리
    private static int scoreCause(String rating, String process) {
        boolean isBad = rating.contains("UNSATISFIED") || rating.contains("SLIGHTLY");
        if (isBad) {
            if (process.isEmpty()) return 0;
            return process.length() > 30 ? 2 : 1;
        }
        return process.isEmpty() ? 1 : 2;
    }

    // ── (4) 다음에 바꿀 것 뚜렷함
    private static int scoreTweakClarity(String adjust) {
        if (adjust.isEmpty()) return 0;
        boolean multi = adjust.contains("\n") || adjust.contains(",")
                     || adjust.contains("/")  || adjust.contains("그리고")
                     || adjust.contains("·")  || adjust.contains("•")
                     || adjust.contains("또");
        return multi ? 2 : 1;
    }

    // ── (5) 다음 시도 준비
    private static int scoreRetryReady(String nextPlan, String retry) {
        if (isHesitantPlan(nextPlan) && retry.isEmpty()) return 0;
        if (retry.isEmpty()) return 0;
        String[] triggers = {"되면","하면","후에","까지","때","다음에","완료","이후","끝나면","마치면"};
        for (String t : triggers) if (retry.contains(t)) return 2;
        return 1;
    }

    // ── (6) 증거/확인 포인트
    private static int scoreEvidence(String url, String checkPoint) {
        if (url.isEmpty()) return 0;
        return checkPoint.isEmpty() ? 1 : 2;
    }

    private static boolean isHesitantPlan(String p) {
        return p.contains("고민") || p.contains("쉬어") || p.contains("보완");
    }

    private static String s(String v) { return v == null ? "" : v.trim(); }
}