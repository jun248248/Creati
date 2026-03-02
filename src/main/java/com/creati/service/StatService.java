package com.creati.service;

import java.util.Map;
/*DB에서 데이터를 긁어오는 역할을 할 서비스 클래스. 
 * 지금은 임시 데이터를 반환하지만, 나중에 이 부분의 SQL 쿼리만 작성하면 됨.*/
public class StatService {
	
	private final com.creati.dao.LogDao logDao = new com.creati.dao.LogDao();
    
    // 주간 기록 추이 데이터를 가져오는 로직 (나중에 DB SELECT 쿼리 들어갈 곳)
    public int[] getWeeklyLogCounts() {
        // TODO: SELECT count(*) FROM logs GROUP BY date...
        return new int[]{3, 5, 2, 8, 4, 7, 6}; // 임시 데이터
    }

    // 카테고리 비율 데이터를 가져오는 로직
    public Map<String, Integer> getCategoryRatios() {
        // TODO: SELECT category, count(*) FROM logs GROUP BY category...
        return Map.of(
            "영상", 45,
            "이미지", 25,
            "글", 20,
            "기타", 10
        );
    }
    
    public int[] getGrowthRadarScores(String userId) {
        // 1) 꾸준함(최근 7일 로그 수)
        int cnt7 = logDao.countLogsLastNDays(userId, 7);

        // 2) 도전력(최근 7일 서로 다른 카테고리 수)
        int cat7 = logDao.countDistinctCategoriesLastNDays(userId, 7);

        // 3) 실행력(최근 30일 SUCCESS 비율)
        double succ30 = logDao.calcSuccessRateLastNDays(userId, 30);

        // 4) 회복력(일단 중)
        int resilience = 2;

        // 5) 성찰력(최근 30일 AI 분석 비율) - ai_analysis.l_id 사용
        double ai30 = logDao.calcAiAnalysisRateLast30Days(userId);

        // 6) 소통력(최근 7일 댓글+공감)
        int social7 = logDao.countSocialLastNDays(userId, 7);

        int consistency = scoreConsistency(cnt7);
        int challenge   = scoreChallenge(cat7);
        int execution   = scoreExecution(succ30);
        int reflection  = scoreReflection(ai30);
        int social      = scoreSocial(social7);

        // 축 순서: 꾸준함, 도전력, 실행력, 회복력, 성찰력, 소통력
        return new int[]{consistency, challenge, execution, resilience, reflection, social};
    }

    // --- 점수 변환(1~3) ---
    private int scoreConsistency(int cnt7) {
        if (cnt7 <= 1) return 1;
        if (cnt7 <= 3) return 2;
        return 3;
    }

    private int scoreChallenge(int distinctCat7) {
        if (distinctCat7 <= 3) return 1;
        if (distinctCat7 <= 7) return 2;
        return 3;
    }

    private int scoreSocial(int social7) {
        if (social7 == 0) return 1;
        if (social7 <= 5) return 2;
        return 3;
    }

    private int scoreExecution(double successRate30) {
        double pct = successRate30 * 100.0;
        if (pct < 30.0) return 1;
        if (pct <= 60.0) return 2;
        return 3;
    }

    private int scoreReflection(double aiRate30) {
        double pct = aiRate30 * 100.0;
        if (pct < 30.0) return 1;
        if (pct <= 60.0) return 2;
        return 3;
    }
    
    public int[] getGrowthRadarScores1(String userId) {
        int cnt7 = logDao.countLogsLastNDays(userId, 7);
        int cat7 = logDao.countDistinctCategoriesLastNDays(userId, 7);
        int social7 = logDao.countSocialLastNDays(userId, 7);

        double succ30 = logDao.calcSuccessRateLastNDays(userId, 30);
        double ai30 = logDao.calcAiAnalysisRateLast30Days(userId);

        int consistency = scoreConsistency(cnt7);
        int challenge   = scoreChallenge(cat7);
        int execution   = scoreExecution(succ30);
        int resilience  = 2; // 회복력은 일단 중
        int reflection  = scoreReflection(ai30);
        int social      = scoreSocial(social7);

        // 축 순서: 꾸준함, 도전력, 실행력, 회복력, 성찰력, 소통력
        return new int[]{consistency, challenge, execution, resilience, reflection, social};
    }
    
    public TypeInfo getTypeByTopAxis(int[] scores) {
        // 축 순서 고정
        String[] axes = {"꾸준함","도전력","실행력","회복력","성찰력","소통력"};

        int topIdx = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[topIdx]) topIdx = i;
        }

        String axis = axes[topIdx];

        return switch (axis) {
            case "꾸준함" -> new TypeInfo("꾸준러형", "매일매일 쌓는 타입");
            case "도전력" -> new TypeInfo("도전가형", "새로운 걸 해봐야 직성이 풀림");
            case "실행력" -> new TypeInfo("실행가형", "시작한 건 끝낸다");
            case "회복력" -> new TypeInfo("리바운더형", "넘어져도 다시 일어난다");
            case "성찰력" -> new TypeInfo("기록가형", "정리하며 성장하는 타입");
            case "소통력" -> new TypeInfo("소통형", "피드백으로 더 커지는 타입");
            default -> new TypeInfo("꾸준러형", "매일매일 쌓는 타입");
        };
    }

    public static class TypeInfo {
        public final String chip;
        public final String desc;
        public TypeInfo(String chip, String desc) {
            this.chip = chip;
            this.desc = desc;
        }
    }
    
}