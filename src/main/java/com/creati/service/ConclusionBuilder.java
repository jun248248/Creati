package com.creati.service;

import com.creati.dto.LogDto;
import com.creati.service.LogCompareScorer.ScoreBreakdown;

import java.util.ArrayList;
import java.util.List;

/** 규칙 기반 결론 문장 생성기 (AI 없음) */
public class ConclusionBuilder {

    public static class Result {
        public final String winner;        // "A" | "B" | "DRAW"
        public final String headline;      // 에티 헤드라인
        public final List<String> reasons; // 이유 최대 3줄
        public final String tip;           // 한 줄 팁
        public final String keepText;      // 유지 카드 내용
        public final String tweakText;     // 조정 카드 내용
        public final boolean isHoldA;      // A가 쉬어갈 상태
        public final boolean isHoldB;      // B가 쉬어갈 상태

        public Result(String winner, String headline, List<String> reasons,
                      String tip, String keepText, String tweakText,
                      boolean isHoldA, boolean isHoldB) {
            this.winner    = winner;
            this.headline  = headline;
            this.reasons   = reasons;
            this.tip       = tip;
            this.keepText  = keepText;
            this.tweakText = tweakText;
            this.isHoldA   = isHoldA;
            this.isHoldB   = isHoldB;
        }
    }

    public static Result build(LogDto a, ScoreBreakdown sa,
                               LogDto b, ScoreBreakdown sb) {
        String winner;
        if (Math.abs(sa.total - sb.total) <= 1) winner = "DRAW";
        else winner = sa.total >= sb.total ? "A" : "B";

        ScoreBreakdown wScore = winner.equals("B") ? sb : sa;
        ScoreBreakdown lScore = winner.equals("B") ? sa : sb;
        LogDto wLog   = winner.equals("B") ? b : a;
        LogDto lLog   = winner.equals("B") ? a : b;

        String headline  = makeHeadline(winner);
        List<String> reasons = makeReasons(winner, wScore, lScore);
        String tip       = makeTip(winner.equals("DRAW") ? sa : lScore,
                                   winner.equals("DRAW") ? a  : lLog);
        String keepText  = makeKeepText(wLog);
        String tweakText = makeTweakText(wLog);

        boolean holdA = s(a.getNextPlanType()).contains("쉬어");
        boolean holdB = s(b.getNextPlanType()).contains("쉬어");

        return new Result(winner, headline, reasons, tip, keepText, tweakText, holdA, holdB);
    }

    // ── 헤드라인
    private static String makeHeadline(String winner) {
        return switch (winner) {
            case "A" -> "에티가 비교해봤어! 이번엔 A가 더 정리 잘 돼 있어";
            case "B" -> "이번엔 B가 더 안정적으로 정리돼 있어 보여";
            default  -> "이번엔 막상막하네! 둘 다 잘 정리돼 있어";
        };
    }

    // ── 이유 2~3줄 (지표 비교 기반)
    private static List<String> makeReasons(String winner,
                                            ScoreBreakdown w, ScoreBreakdown l) {
        List<String> list = new ArrayList<>();
        String n = winner.equals("DRAW") ? "둘 다" : winner;

        if (winner.equals("DRAW")) {
            list.add("A랑 B 둘 다 잘 정리돼 있어!");
            list.add("크게 차이는 없어. 둘 다 충분히 좋아");
            list.add("기록은 이미 잘하고 있어. 이제 조금씩 다듬어보자!");
            return list;
        }

        if (w.decision > l.decision)
            list.add(n + "는 다음에 뭘 할지 더 또렷하게 정리돼 있어!");
        if (w.planFit > l.planFit)
            list.add(n + "는 계획이랑 실제 흐름이 잘 맞아 보여");
        if (w.tweakClarity > l.tweakClarity)
            list.add(n + "는 바꿀 포인트가 딱 정리돼 있어서 다시 시도하기 쉬워!");
        if (w.retryReady > l.retryReady)
            list.add(n + "는 다음 시도 준비가 더 잘 돼 있어!");
        if (w.evidence > l.evidence)
            list.add(n + "는 확인 포인트가 있어서 비교하기 편해!");
        if (w.cause > l.cause)
            list.add(n + "는 이유 정리가 잘 돼 있어서 다음 비교도 쉬울 거야!");

        if (list.isEmpty())
            list.add(n + " 방식이 이어가기 더 편해 보여");

        return list.subList(0, Math.min(3, list.size()));
    }

    // ── 한 줄 팁 (가장 약한 지표 기준)
    private static String makeTip(ScoreBreakdown weak, LogDto log) {
        String nextPlan = s(log.getNextPlanType());

        if (weak.tweakClarity == 0)
            return "다음엔 바꿀 것 한 가지만 딱 적어보자!";

        boolean isHesitant = nextPlan.contains("고민") || nextPlan.contains("쉬어") || nextPlan.contains("보완");
        if (weak.retryReady == 0 && isHesitant)
            return "고민 중이면 '언제 다시 할지' 한 줄만 적어보자!";

        if (weak.evidence == 0)
            return "링크가 있다면 확인할 포인트도 한 줄 남겨보자!";

        String rating = s(log.getResultRating());
        boolean isBad = rating.contains("UNSATISFIED") || rating.contains("SLIGHTLY");
        if (weak.cause == 0 && isBad)
            return "아쉬웠다면 이유 한 가지만 적어도 큰 도움이 돼!";

        return "오늘도 한 걸음 정리 완료. 기록은 이미 잘하고 있어!";
    }

    // ── 유지 카드: process(good/잘 된 부분) 첫 줄
    private static String makeKeepText(LogDto log) {
        String text = s(log.getProcess());
        if (!text.isEmpty()) return firstLine(text);
        return "잘 된 부분을 다음에도 가져가 보자!";
    }

    // ── 조정 카드: nextAdjust(reflection) 첫 줄
    private static String makeTweakText(LogDto log) {
        String text = s(log.getReflection());
        if (!text.isEmpty()) return firstLine(text);
        return "다음엔 이 포인트만 실험해보면 어때?";
    }

    private static String firstLine(String text) {
        String[] parts = text.split("[\n\r]");
        return parts[0].trim();
    }

    private static String s(String v) { return v == null ? "" : v.trim(); }
}