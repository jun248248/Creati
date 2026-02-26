package com.creati.service;

import java.util.Map;
/*DB에서 데이터를 긁어오는 역할을 할 서비스 클래스. 
 * 지금은 임시 데이터를 반환하지만, 나중에 이 부분의 SQL 쿼리만 작성하면 됨.*/
public class StatService {
    
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
}