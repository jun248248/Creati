package com.creati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.creati.database.DBConnectionMgr;
import com.creati.dto.CategoryDto;
import com.creati.dto.LogDto;
import com.creati.dto.MyLogListDto;
import com.creati.dto.PublicLogListDto;

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
	                i.i_name
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
	                l_id,
	                l_title,
	                u_id,
	                l_result_status,
	                created_at
	            FROM log
	            WHERE l_is_public = 1
	              AND l_is_draft = 0
	            ORDER BY created_at DESC
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

    // =========================
    // SELECT: by l_id
    // =========================
    public LogDto findById(long logId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LogDto dto = null;

        String sql = """
            SELECT
                l_id, u_id, l_title, i_id, c_id,
                l_result_status, l_is_public, l_is_draft,
                l_content_url, l_goal, l_result_rating,
                l_process, l_plan_difference, l_difference, l_reflection,
                next_plan_type, retry_condition, created_at
            FROM log
            WHERE l_id = ?
            """;

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, logId);

            rs = pstmt.executeQuery();
            if (rs.next()) dto = mapRow(rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt, rs);
        }

        return dto;
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
    
    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
