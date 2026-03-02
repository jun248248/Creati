package com.creati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

import com.creati.database.DBConnectionMgr;
import com.creati.dto.CategoryDto;
import com.creati.dto.LogDto;
import com.creati.dto.MyLogListDto;
import com.creati.dto.PublicLogListDto;
import com.creati.model.LogPost;
import com.creati.model.LogStatus;


public class LogDao {

	private DBConnectionMgr pool;

	public LogDao() {
		pool = DBConnectionMgr.getInstance();
	}

	/*
	 내 로그 리스트 조회 (요약용)
	 */
	public List<MyLogListDto> findMyLogList(String userId, Long categoryId) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    List<MyLogListDto> list = new ArrayList<>();

	    // LEFT JOIN을 사용하여 카테고리(interest) 정보가 없어도 로그는 나오도록 함
	    StringBuilder sql = new StringBuilder("""
	            SELECT 
	                l.l_id,
	                l.l_result_status,
	                l.l_title,
	                l.created_at,
	                i.i_name,
	                l.l_is_public
	            FROM log l
	            LEFT JOIN interest i ON l.i_id = i.i_id
	            WHERE l.u_id = ?
	            """);

	    if (categoryId != null && categoryId > 0) {
	        sql.append(" AND l.i_id = ? ");
	    }
	    sql.append(" ORDER BY l.created_at DESC");

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql.toString());
	        
	        // 디버깅: 현재 어떤 ID로 조회하는지 출력
	        System.out.println(">>> DAO 조회 ID: [" + userId + "]"); 
	        
	        pstmt.setString(1, userId);
	        if (categoryId != null && categoryId > 0) {
	            pstmt.setLong(2, categoryId);
	        }

	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	            MyLogListDto dto = new MyLogListDto();
	            dto.setId(rs.getLong("l_id"));
	            dto.setResultStatus(rs.getString("l_result_status"));
	            dto.setTitle(rs.getString("l_title"));
	            dto.setCategoryName(rs.getString("i_name") != null ? rs.getString("i_name") : "미지정");
	            dto.setPublic(rs.getBoolean("l_is_public"));

	            if (rs.getTimestamp("created_at") != null) {
	                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
	            }
	            list.add(dto);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }
	    return list;
	}
	
	/*
	 전체 공개 게시글 목록 조회
	 */
	public List<PublicLogListDto> findAllPublicLogs() {

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    List<PublicLogListDto> list = new ArrayList<>();

	    String sql = """
	    	    SELECT
	    	        l.l_id,
	    	        l.l_title,
	    	        l.u_id,
	    	        l.l_result_status,
	    	        l.created_at,
	    	        i.i_name
	    	    FROM log l
	    	    LEFT JOIN interest i ON l.i_id = i.i_id
	    	    WHERE l.l_is_public = 1
	    	      AND l.l_is_draft = 0
	    	    ORDER BY l.created_at DESC
	    	    """;

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);

	        rs = pstmt.executeQuery();

	        while (rs.next()) {

	            PublicLogListDto dto = new PublicLogListDto();

	            dto.setId(rs.getLong("l_id"));
	            dto.setTitle(rs.getString("l_title"));
	            dto.setUserId(rs.getString("u_id"));
	            dto.setResultStatus(rs.getString("l_result_status"));
	            dto.setFieldName(rs.getString("i_name") != null ? rs.getString("i_name") : "기타");
	            
	            if (rs.getTimestamp("created_at") != null) {
	                dto.setCreatedAt(
	                        rs.getTimestamp("created_at").toLocalDateTime()
	                );
	            }

	            list.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }

	    return list;
	}
	
	//카테고리별 조회
	public List<CategoryDto> findAllCategories() {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    List<com.creati.dto.CategoryDto> list = new ArrayList<>();

	    String sql = """
	            SELECT i_id, i_name 
	            FROM interest 
	            ORDER BY i_id ASC
	            """;

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            com.creati.dto.CategoryDto dto = new com.creati.dto.CategoryDto();
	            dto.setId(rs.getLong("i_id"));       
	            dto.setName(rs.getString("i_name")); 
	            list.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }

	    return list;
	}
	
	//유저별 카테고리 조회
	public List<CategoryDto> findUsedCategoriesByUserId(String userId) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    List<com.creati.dto.CategoryDto> list = new ArrayList<>();

	    // 글이 있는 카테고리만 가져옴
	    String sql = """
	            SELECT DISTINCT i.i_id, i.i_name
	            FROM interest i
	            JOIN log l ON i.i_id = l.i_id
	            WHERE l.u_id = ?
	            ORDER BY i.i_id ASC
	            """;

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, userId); // 유저 ID 세팅
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            com.creati.dto.CategoryDto dto = new com.creati.dto.CategoryDto();
	            dto.setId(rs.getLong("i_id"));       
	            dto.setName(rs.getString("i_name")); 
	            list.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }

	    return list;
	}
	
	// =========================
    // INSERT (returns new l_id)
    // =========================
    public long insertLog(LogDto dto) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        long generatedId = -1;

        String sql = """
            INSERT INTO log (
                u_id, l_title, i_id, c_id, l_result_status,
                l_is_public, l_is_draft, l_content_url, l_goal, l_result_rating,
                l_process, l_plan_difference, l_difference, l_reflection,
                next_plan_type, retry_condition, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            """;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 1~4
            pstmt.setString(1, dto.getUserId());
            pstmt.setString(2, dto.getTitle());
            pstmt.setLong(3, dto.getInterestId() != null ? dto.getInterestId() : 0L); // i_id
            pstmt.setLong(4, dto.getCategoryId() != null ? dto.getCategoryId() : 0L); // c_id

            // 5~7
            pstmt.setString(5, dto.getResultStatus()); // DB ENUM 문자열
            pstmt.setBoolean(6, dto.getIsPublic() != null ? dto.getIsPublic() : false);
            pstmt.setBoolean(7, dto.getIsDraft() != null ? dto.getIsDraft() : false);

            // 8~16
            pstmt.setString(8,  nullIfBlank(dto.getContentUrl()));
            pstmt.setString(9,  nullIfBlank(dto.getGoal()));
            pstmt.setString(10, dto.getResultRating()); // DB ENUM 문자열
            pstmt.setString(11, nullIfBlank(dto.getProcess()));
            pstmt.setString(12, dto.getPlanDifference()); // DB ENUM 문자열
            pstmt.setString(13, nullIfBlank(dto.getDifference()));     // NULL 허용이면 null 가능
            pstmt.setString(14, nullIfBlank(dto.getReflection()));
            pstmt.setString(15, dto.getNextPlanType());
            pstmt.setString(16, nullIfBlank(dto.getRetryCondition())); // NULL 허용이면 null 가능

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) generatedId = rs.getLong(1);
            }

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            pool.freeConnection(conn, pstmt, rs);
        }

        return generatedId;
    }

    // =========================
    // UPDATE (by l_id + u_id)
    // =========================
    public boolean updateLog(LogDto dto) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        boolean ok = false;

        String sql = """
            UPDATE log
            SET
                l_title = ?,
                i_id = ?,
                c_id = ?,
                l_result_status = ?,
                l_is_public = ?,
                l_is_draft = ?,
                l_content_url = ?,
                l_goal = ?,
                l_result_rating = ?,
                l_process = ?,
                l_plan_difference = ?,
                l_difference = ?,
                l_reflection = ?,
                next_plan_type = ?,
                retry_condition = ?
            WHERE l_id = ?
              AND u_id = ?
            """;

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, dto.getTitle());
            pstmt.setLong(2, dto.getInterestId() != null ? dto.getInterestId() : 0L);
            pstmt.setLong(3, dto.getCategoryId() != null ? dto.getCategoryId() : 0L);

            pstmt.setString(4, dto.getResultStatus());
            pstmt.setBoolean(5, dto.getIsPublic() != null ? dto.getIsPublic() : false);
            pstmt.setBoolean(6, dto.getIsDraft() != null ? dto.getIsDraft() : false);

            pstmt.setString(7, dto.getContentUrl());
            pstmt.setString(8, dto.getGoal());
            pstmt.setString(9, dto.getResultRating());
            pstmt.setString(10, dto.getProcess());
            pstmt.setString(11, dto.getPlanDifference());
            pstmt.setString(12, dto.getDifference());
            pstmt.setString(13, dto.getReflection());
            pstmt.setString(14, dto.getNextPlanType());
            pstmt.setString(15, dto.getRetryCondition());

            pstmt.setLong(16, dto.getId());
            pstmt.setString(17, dto.getUserId());

            ok = pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt);
        }

        return ok;
    }

    // =========================
    // DELETE (by l_id + u_id)
    // =========================
    public boolean deleteLog(long logId, String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        boolean ok = false;

        String sql = """
            DELETE FROM log
            WHERE l_id = ?
              AND u_id = ?
            """;

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, logId);
            pstmt.setString(2, userId);

            ok = pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt);
        }

        return ok;
    }

    //로그 id로 상세 조회
    public LogPost findPostById(long logId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();

            // 1) log + interest + category (이름까지)
            String sql = """
                SELECT
                    l.l_id,
                    l.l_title,
                    l.u_id,
                    l.i_id,
                    i.i_name AS interest_name,
                    l.c_id,
                    c.c_name AS category_name,
                    l.l_result_status,
                    l.l_is_public,
                    l.l_is_draft,
                    l.l_content_url,
                    l.l_goal,
                    l.l_result_rating,
                    l.l_process,
                    l.l_plan_difference,
                    l.l_difference,
                    l.l_reflection,
                    l.next_plan_type,
                    l.retry_condition,
                    l.created_at
                FROM log l
                LEFT JOIN interest i ON l.i_id = i.i_id
                LEFT JOIN category c ON l.c_id = c.c_id
                WHERE l.l_id = ?
                """;

            ps = conn.prepareStatement(sql);
            ps.setLong(1, logId);
            rs = ps.executeQuery();

            if (!rs.next()) return null;

            long lId = rs.getLong("l_id");
            String title = rs.getString("l_title");

            String field = rs.getString("interest_name");   // i_name
            String subCategory = rs.getString("category_name"); // c_name

            String dbStatus = rs.getString("l_result_status"); // SUCCESS/FAIL/ONGOING
            LogStatus status = mapDbStatusToLogStatus(dbStatus);

            boolean isPublic = rs.getBoolean("l_is_public");

            // created_at -> LocalDate (LogPost가 LocalDate를 쓰고 있음)
            LocalDate createdAt = LocalDate.now();
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) createdAt = ts.toLocalDateTime().toLocalDate();

            // 본문 필드들
            String goalText = rs.getString("l_goal");
            String mood = mapDbRatingToMoodKorean(rs.getString("l_result_rating")); // VERY_SATISFIED -> 만족해요 ...
            String processText = rs.getString("l_process");
            String planGapLevel = mapDbPlanDiffToKorean(rs.getString("l_plan_difference")); // SIMILAR -> 거의 비슷해요 ...
            String planGapDetail = rs.getString("l_difference");
            String learningText = rs.getString("l_reflection");
            String nextPlan = rs.getString("next_plan_type");
            String retryCondition = rs.getString("retry_condition");

            String linkUrl = rs.getString("l_content_url");

            // 2) 조인 테이블: good / influence / adjustment (문구 리스트로)
            List<String> goodPoints = fetchGoodPoints(conn, lId);
            List<String> influenceFactors = fetchInfluenceFactors(conn, lId);
            List<String> nextAdjustPoints = fetchAdjustmentPoints(conn, lId);

            // ※ 아래 3개(other 텍스트)는 DB 컬럼이 따로 없으면 null로 둠
            String goodOther = null;
            String influenceOther = null;
            String nextAdjustOther = null;

            // ※ painPoint(아쉬움 한줄)도 DB 컬럼이 따로 없으면 null로 둠
            String painPoint = null;

            // linkPoint도 별도 컬럼 없으면 null
            String linkPoint = null;

            // LogPost 생성 (WriteLogView.toLogPost()에서 쓰는 v2 생성자 시그니처 그대로)
            return new LogPost(
                "LOG",
                String.valueOf(lId),
                safe(field),
                safe(subCategory),
                status,
                safe(title),
                createdAt,
                isPublic,

                safe(goalText),
                safe(mood),
                goodPoints,
                goodOther,

                painPoint,
                influenceFactors,
                influenceOther,

                safe(processText),
                safe(planGapLevel),
                safe(planGapDetail),
                safe(learningText),

                nextAdjustPoints,
                nextAdjustOther,

                safe(nextPlan),
                safe(retryCondition),
                safe(linkUrl),
                linkPoint
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    // ===== LogDao 안에 "추가" (조인 리스트 조회 3개) =====
    private List<String> fetchGoodPoints(Connection conn, long logId) throws Exception {
        String sql = """
            SELECT gp.gp_name
            FROM log_good_point lgp
            JOIN good_point gp ON lgp.gp_id = gp.gp_id
            WHERE lgp.l_id = ?
            ORDER BY gp.gp_id ASC
            """;
        return fetchStringList(conn, sql, logId);
    }

    private List<String> fetchInfluenceFactors(Connection conn, long logId) throws Exception {
        String sql = """
            SELECT f.if_name
            FROM log_influence_factor lif
            JOIN influence_factor f ON lif.if_id = f.if_id
            WHERE lif.l_id = ?
            ORDER BY f.if_id ASC
            """;
        return fetchStringList(conn, sql, logId);
    }

    private List<String> fetchAdjustmentPoints(Connection conn, long logId) throws Exception {
        String sql = """
            SELECT ap.ap_name
            FROM log_adjustment_point lap
            JOIN adjustment_point ap ON lap.ap_id = ap.ap_id
            WHERE lap.l_id = ?
            ORDER BY ap.ap_id ASC
            """;
        return fetchStringList(conn, sql, logId);
    }

    private List<String> fetchStringList(Connection conn, String sql, long logId) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, logId);
            rs = ps.executeQuery();

            List<String> list = new ArrayList<>();
            while (rs.next()) {
                String v = rs.getString(1);
                if (v != null && !v.trim().isEmpty()) list.add(v.trim());
            }
            return list;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
        }
    }

    // ===== LogDao 안에 "추가" (ENUM 매핑들) =====
    private LogStatus mapDbStatusToLogStatus(String db) {
        if (db == null) return LogStatus.IN_PROGRESS;
        return switch (db.trim()) {
            case "SUCCESS" -> LogStatus.DONE;
            case "FAIL" -> LogStatus.NEEDS_IMPROVEMENT;
            case "ONGOING" -> LogStatus.IN_PROGRESS;
            default -> LogStatus.IN_PROGRESS;
        };
    }

    private String mapDbRatingToMoodKorean(String db) {
        if (db == null) return "";
        return switch (db.trim()) {
            case "VERY_SATISFIED" -> "만족해요";
            case "SATISFIED" -> "괜찮아요";
            case "SLIGHTLY_DISAPPOINTED" -> "조금 아쉬워요";
            case "VERY_DISAPPOINTED" -> "많이 아쉬워요";
            default -> "";
        };
    }

    private String mapDbPlanDiffToKorean(String db) {
        if (db == null) return "";
        return switch (db.trim()) {
            case "SIMILAR" -> "거의 비슷해요";
            case "PARTIAL_DIFF" -> "일부 달라요";
            case "VERY_DIFF" -> "많이 달라요";
            default -> "";
        };
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    // =========================
    // SELECT: by u_id (optionally drafts)
    // =========================
    public List<LogDto> findByUserId(String userId, Boolean isDraft) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<LogDto> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                l_id, u_id, l_title, i_id, c_id,
                l_result_status, l_is_public, l_is_draft,
                l_content_url, l_goal, l_result_rating,
                l_process, l_plan_difference, l_difference, l_reflection,
                next_plan_type, retry_condition, created_at
            FROM log
            WHERE u_id = ?
            """);

        if (isDraft != null) {
            sql.append(" AND l_is_draft = ? ");
        }
        sql.append(" ORDER BY created_at DESC ");

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, userId);
            if (isDraft != null) {
                pstmt.setBoolean(2, isDraft);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt, rs);
        }

        return list;
    }

    // =========================
    // SELECT: public logs
    // =========================
    public List<LogDto> findPublicLogs(int limit, int offset) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<LogDto> list = new ArrayList<>();

        String sql = """
            SELECT
                l_id, u_id, l_title, i_id, c_id,
                l_result_status, l_is_public, l_is_draft,
                l_content_url, l_goal, l_result_rating,
                l_process, l_plan_difference, l_difference, l_reflection,
                next_plan_type, retry_condition, created_at
            FROM log
            WHERE l_is_public = 1
              AND l_is_draft = 0
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Math.max(1, limit));
            pstmt.setInt(2, Math.max(0, offset));

            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt, rs);
        }

        return list;
    }
    
 // =========================
    // mapper
    // =========================
    private LogDto mapRow(ResultSet rs) throws Exception {
        LogDto dto = new LogDto();

        dto.setId(rs.getLong("l_id"));
        dto.setUserId(rs.getString("u_id"));
        dto.setTitle(rs.getString("l_title"));

        dto.setInterestId(rs.getLong("i_id"));
        dto.setCategoryId(rs.getLong("c_id"));

        dto.setResultStatus(rs.getString("l_result_status"));
        dto.setIsPublic(rs.getBoolean("l_is_public"));
        dto.setIsDraft(rs.getBoolean("l_is_draft"));

        dto.setContentUrl(rs.getString("l_content_url"));
        dto.setGoal(rs.getString("l_goal"));
        dto.setResultRating(rs.getString("l_result_rating"));
        dto.setProcess(rs.getString("l_process"));

        dto.setPlanDifference(rs.getString("l_plan_difference"));
        dto.setDifference(rs.getString("l_difference"));
        dto.setReflection(rs.getString("l_reflection"));

        dto.setNextPlanType(rs.getString("next_plan_type"));
        dto.setRetryCondition(rs.getString("retry_condition"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) dto.setCreatedAt(ts.toLocalDateTime());

        return dto;
    }
    
 
    //null 처리
    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    
    private long ensureGoodPointId(String name) throws SQLException {
        String n = nullIfBlank(name);
        if (n == null) throw new SQLException("good point name blank");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();

            ps = conn.prepareStatement("SELECT gp_id FROM good_point WHERE gp_name = ?");
            ps.setString(1, n);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);

            rs.close(); ps.close();

            ps = conn.prepareStatement("INSERT INTO good_point (gp_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, n);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

            throw new SQLException("no generated key for good_point");
        } catch (Exception e) {
            throw new SQLException("ensureGoodPointId failed: " + n, e);
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    private long ensureInfluenceFactorId(String name) throws SQLException {
        String n = nullIfBlank(name);
        if (n == null) throw new SQLException("influence factor name blank");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();

            ps = conn.prepareStatement("SELECT if_id FROM influence_factor WHERE if_name = ?");
            ps.setString(1, n);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);

            rs.close(); ps.close();

            ps = conn.prepareStatement("INSERT INTO influence_factor (if_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, n);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

            throw new SQLException("no generated key for influence_factor");
        } catch (Exception e) {
            throw new SQLException("ensureInfluenceFactorId failed: " + n, e);
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    private long ensureAdjustmentPointId(String name) throws SQLException {
        String n = nullIfBlank(name);
        if (n == null) throw new SQLException("adjustment point name blank");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();

            ps = conn.prepareStatement("SELECT ap_id FROM adjustment_point WHERE ap_name = ?");
            ps.setString(1, n);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);

            rs.close(); ps.close();

            ps = conn.prepareStatement("INSERT INTO adjustment_point (ap_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, n);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

            throw new SQLException("no generated key for adjustment_point");
        } catch (Exception e) {
            throw new SQLException("ensureAdjustmentPointId failed: " + n, e);
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    public void insertGoodPoints(long logId, java.util.List<String> points) throws SQLException {
        if (points == null || points.isEmpty()) return;

        String sql = "INSERT INTO log_good_point (l_id, gp_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);

            for (String p : points) {
                String v = nullIfBlank(p);
                if (v == null) continue;

                long gpId = ensureGoodPointId(v);
                ps.setLong(1, logId);
                ps.setLong(2, gpId);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (Exception e) {
            throw new SQLException("insertGoodPoints failed", e);
        } finally {
            pool.freeConnection(conn, ps);
        }
    }

    public void insertInfluenceFactors(long logId, java.util.List<String> factors) throws SQLException {
        if (factors == null || factors.isEmpty()) return;

        String sql = "INSERT INTO log_influence_factor (l_id, if_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);

            for (String f : factors) {
                String v = nullIfBlank(f);
                if (v == null) continue;

                long ifId = ensureInfluenceFactorId(v);
                ps.setLong(1, logId);
                ps.setLong(2, ifId);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (Exception e) {
            throw new SQLException("insertInfluenceFactors failed", e);
        } finally {
            pool.freeConnection(conn, ps);
        }
    }

    public void insertAdjustmentPoints(long logId, java.util.List<String> points) throws SQLException {
        if (points == null || points.isEmpty()) return;

        String sql = "INSERT INTO log_adjustment_point (l_id, ap_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);

            for (String p : points) {
                String v = nullIfBlank(p);
                if (v == null) continue;

                long apId = ensureAdjustmentPointId(v);
                ps.setLong(1, logId);
                ps.setLong(2, apId);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (Exception e) {
            throw new SQLException("insertAdjustmentPoints failed", e);
        } finally {
            pool.freeConnection(conn, ps);
        }
    }
    
    public int countMyLogsThisMonth(String userId) {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM log
            WHERE u_id = ?
              AND l_is_draft = 0
              AND created_at >= ?
              AND created_at < ?
            """;

        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;

        try {
            java.time.LocalDate first = java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate next = first.plusMonths(1);

            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(first.atStartOfDay()));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(next.atStartOfDay()));

            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
        return 0;
    }

    public int countMyDistinctCategoriesThisMonth(String userId) {
        String sql = """
            SELECT COUNT(DISTINCT c_id) AS cnt
            FROM log
            WHERE u_id = ?
              AND l_is_draft = 0
              AND c_id IS NOT NULL
              AND created_at >= ?
              AND created_at < ?
            """;

        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;

        try {
            java.time.LocalDate first = java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate next = first.plusMonths(1);

            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(first.atStartOfDay()));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(next.atStartOfDay()));

            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
        return 0;
    }

    public String findMyTopFieldThisMonth(String userId) {
        // 대표 분야 = 이번 달 로그에서 i_id(interest)가 가장 많이 나온 i_name
        // 공개/비공개 상관없이 포함, 임시저장 제외
        String sql = """
            SELECT i.i_name, COUNT(*) AS cnt
            FROM log l
            LEFT JOIN interest i ON l.i_id = i.i_id
            WHERE l.u_id = ?
              AND l.l_is_draft = 0
              AND l.created_at >= ?
              AND l.created_at < ?
            GROUP BY l.i_id, i.i_name
            ORDER BY cnt DESC
            LIMIT 1
            """;

        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;

        try {
            java.time.LocalDate first = java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate next = first.plusMonths(1);

            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(first.atStartOfDay()));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(next.atStartOfDay()));

            rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("i_name");
                return (name == null || name.isBlank()) ? "기타" : name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
        return "기타";
    }
    
    public boolean updateLogWithExtras(LogDto dto, com.creati.ui.main.WriteLogView.Draft d) {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) return false;
        if (dto.getUserId() == null || dto.getUserId().isBlank()) return false;

        Connection conn = null;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            // 1) log 본문 UPDATE (u_id까지 걸어서 보호)
            boolean ok = updateLogTx(conn, dto);
            if (!ok) {
                conn.rollback();
                return false;
            }

            long logId = dto.getId();

            // 2) 조인 테이블 교체(삭제 후 재삽입)
            // 조정 포인트는 항상 저장
            replaceAdjustmentPointsTx(conn, logId, d.nextAdjustPoints, d.nextAdjustOther);

            // 결과 인식(만족/괜찮이면 good, 아쉬우면 influence)
            boolean positive = isPositiveMood(d.mood);

            if (positive) {
                // good 저장, influence는 비움
                replaceGoodPointsTx(conn, logId, d.goodPoints, d.goodOther);
                clearInfluenceFactorsTx(conn, logId);
            } else {
                // influence 저장, good은 비움
                replaceInfluenceFactorsTx(conn, logId, d.influenceFactors, d.influenceOther);
                clearGoodPointsTx(conn, logId);
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            try { if (conn != null) pool.freeConnection(conn); } catch (Exception ignore) {}
        }
    }

    /** mood가 "만족해요/괜찮아요"면 true */
    private boolean isPositiveMood(String mood) {
        if (mood == null) return false;
        String m = mood.trim();
        return "만족해요".equals(m) || "괜찮아요".equals(m);
    }

    /** 같은 커넥션으로 log 본문 UPDATE */
    private boolean updateLogTx(Connection conn, LogDto dto) throws SQLException {
        String sql = """
            UPDATE log SET
                l_title = ?,
                i_id = ?,
                c_id = ?,
                l_result_status = ?,
                l_is_public = ?,
                l_is_draft = ?,
                l_content_url = ?,
                l_goal = ?,
                l_result_rating = ?,
                l_process = ?,
                l_plan_difference = ?,
                l_difference = ?,
                l_reflection = ?,
                next_plan_type = ?,
                retry_condition = ?
            WHERE l_id = ?
              AND u_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullIfBlank(dto.getTitle()));

            if (dto.getInterestId() != null && dto.getInterestId() > 0) ps.setLong(2, dto.getInterestId());
            else ps.setNull(2, Types.BIGINT);

            if (dto.getCategoryId() != null && dto.getCategoryId() > 0) ps.setLong(3, dto.getCategoryId());
            else ps.setNull(3, Types.BIGINT);

            ps.setString(4, nullIfBlank(dto.getResultStatus()));
            ps.setBoolean(5, dto.getIsPublic() != null ? dto.getIsPublic() : true);
            ps.setBoolean(6, dto.getIsDraft() != null ? dto.getIsDraft() : false);

            ps.setString(7, nullIfBlank(dto.getContentUrl()));
            ps.setString(8, nullIfBlank(dto.getGoal()));
            ps.setString(9, nullIfBlank(dto.getResultRating()));

            ps.setString(10, nullIfBlank(dto.getProcess()));
            ps.setString(11, nullIfBlank(dto.getPlanDifference()));
            ps.setString(12, nullIfBlank(dto.getDifference()));
            ps.setString(13, nullIfBlank(dto.getReflection()));

            ps.setString(14, nullIfBlank(dto.getNextPlanType()));
            ps.setString(15, nullIfBlank(dto.getRetryCondition()));

            ps.setLong(16, dto.getId());
            ps.setString(17, dto.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    /* =========================
     * GOOD POINT (log_good_point)
     * ========================= */

    private void clearGoodPointsTx(Connection conn, long logId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM log_good_point WHERE l_id=?")) {
            ps.setLong(1, logId);
            ps.executeUpdate();
        }
    }

    private void replaceGoodPointsTx(Connection conn, long logId, List<String> points, String other) throws SQLException {
        clearGoodPointsTx(conn, logId);

        List<String> all = new ArrayList<>();
        if (points != null) all.addAll(points);
        String o = nullIfBlank(other);
        if (o != null) all.add(o);

        if (all.isEmpty()) return;

        String sql = "INSERT INTO log_good_point (l_id, gp_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String name : all) {
                String v = nullIfBlank(name);
                if (v == null) continue;

                long gpId = ensureGoodPointIdTx(conn, v);
                ps.setLong(1, logId);
                ps.setLong(2, gpId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private long ensureGoodPointIdTx(Connection conn, String gpName) throws SQLException {
        // good_point(gp_id, gp_name)
        Long found = selectOneLongTx(conn, "SELECT gp_id FROM good_point WHERE gp_name = ?", gpName);
        if (found != null) return found;
        return insertAndReturnKeyTx(conn, "INSERT INTO good_point (gp_name) VALUES (?)", gpName);
    }

    /* =========================
     * INFLUENCE FACTOR (log_influence_factor)
     * ========================= */

    private void clearInfluenceFactorsTx(Connection conn, long logId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM log_influence_factor WHERE l_id=?")) {
            ps.setLong(1, logId);
            ps.executeUpdate();
        }
    }

    private void replaceInfluenceFactorsTx(Connection conn, long logId, List<String> factors, String other) throws SQLException {
        clearInfluenceFactorsTx(conn, logId);

        List<String> all = new ArrayList<>();
        if (factors != null) all.addAll(factors);
        String o = nullIfBlank(other);
        if (o != null) all.add(o);

        if (all.isEmpty()) return;

        String sql = "INSERT INTO log_influence_factor (l_id, if_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String name : all) {
                String v = nullIfBlank(name);
                if (v == null) continue;

                long ifId = ensureInfluenceFactorIdTx(conn, v);
                ps.setLong(1, logId);
                ps.setLong(2, ifId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private long ensureInfluenceFactorIdTx(Connection conn, String ifName) throws SQLException {
        // influence_factor(if_id, if_name)
        Long found = selectOneLongTx(conn, "SELECT if_id FROM influence_factor WHERE if_name = ?", ifName);
        if (found != null) return found;
        return insertAndReturnKeyTx(conn, "INSERT INTO influence_factor (if_name) VALUES (?)", ifName);
    }

    /* =========================
     * ADJUSTMENT POINT (log_adjustment_point)
     * ========================= */

    private void replaceAdjustmentPointsTx(Connection conn, long logId, List<String> points, String other) throws SQLException {
        // delete
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM log_adjustment_point WHERE l_id=?")) {
            ps.setLong(1, logId);
            ps.executeUpdate();
        }

        List<String> all = new ArrayList<>();
        if (points != null) all.addAll(points);
        String o = nullIfBlank(other);
        if (o != null) all.add(o);

        if (all.isEmpty()) return;

        String sql = "INSERT INTO log_adjustment_point (l_id, ap_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String name : all) {
                String v = nullIfBlank(name);
                if (v == null) continue;

                long apId = ensureAdjustmentPointIdTx(conn, v);
                ps.setLong(1, logId);
                ps.setLong(2, apId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private long ensureAdjustmentPointIdTx(Connection conn, String apName) throws SQLException {
        // adjustment_point(ap_id, ap_name)
        Long found = selectOneLongTx(conn, "SELECT ap_id FROM adjustment_point WHERE ap_name = ?", apName);
        if (found != null) return found;
        return insertAndReturnKeyTx(conn, "INSERT INTO adjustment_point (ap_name) VALUES (?)", apName);
    }

    /* =========================
     * 공통 helper (TX)
     * ========================= */

    private Long selectOneLongTx(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                return null;
            }
        }
    }

    private long insertAndReturnKeyTx(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, param);
            int a = ps.executeUpdate();
            if (a <= 0) throw new SQLException("insert failed: " + sql);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("no generated key: " + sql);
    }
    
    public boolean deleteLogWithExtras(long logId, String userId) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            // 1) 자식 테이블(댓글/리액션) 먼저
            execDelete(conn, "DELETE FROM reply WHERE l_id = ?", logId);
            execDelete(conn, "DELETE FROM log_reaction WHERE l_id = ?", logId);

            // 2) 로그 부가(조인) 먼저
            execDelete(conn, "DELETE FROM log_good_point WHERE l_id = ?", logId);
            execDelete(conn, "DELETE FROM log_influence_factor WHERE l_id = ?", logId);
            execDelete(conn, "DELETE FROM log_adjustment_point WHERE l_id = ?", logId);

            // 3) 마지막에 본문(log) 삭제 (내 글만 보호)
            //
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM log WHERE l_id = ? AND u_id = ?")) {
                ps.setLong(1, logId);
                ps.setString(2, userId);

                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            pool.freeConnection(conn);
        }
    }

    private void execDelete(Connection conn, String sql, long logId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, logId);
            ps.executeUpdate();
        }
    }
    
}
