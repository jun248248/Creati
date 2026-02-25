package com.creati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.creati.database.DBConnectionMgr;
import com.creati.dto.LogDto;
import com.creati.dto.MyLogListDto;
import com.creati.dto.PublicLogListDto;

public class LogDao {

	private DBConnectionMgr pool;

	public LogDao() {
		pool = DBConnectionMgr.getInstance();
	}

	/*
	 * 작성자 ID로 게시글 목록 조회
	 */
	public List<LogDto> findByUserId(String userId) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<LogDto> list = new ArrayList<>();

		String sql = """
				SELECT *
				FROM log
				WHERE u_id = ?
				ORDER BY l_created_at DESC
				""";

		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, userId);

			rs = pstmt.executeQuery();

			while (rs.next()) {

				LogDto dto = new LogDto();

				dto.setId(rs.getLong("l_id"));
				dto.setUserId(rs.getString("u_id"));
				dto.setTitle(rs.getString("l_title"));
				dto.setContentTitle(rs.getString("l_content_title"));
				dto.setContentUrl(rs.getString("l_content_url"));
				dto.setPlatformId(rs.getLong("pf_id"));
				dto.setCategoryId(rs.getLong("c_id"));
				dto.setTryContent(rs.getString("l_try_content"));
				dto.setResultStatus(rs.getString("l_result_status"));
				dto.setFailResult(rs.getString("l_fail_result"));
				dto.setFailReason(rs.getString("l_failure_reason"));
				dto.setIsPublic(rs.getBoolean("l_is_public"));
				dto.setIsDraft(rs.getBoolean("l_is_draft"));
				dto.setViewCount(rs.getLong("l_view_count"));

				if (rs.getTimestamp("l_created_at") != null) {
					dto.setCreatedAt(rs.getTimestamp("l_created_at").toLocalDateTime());
				}

				if (rs.getTimestamp("l_updated_at") != null) {
					dto.setUpdatedAt(rs.getTimestamp("l_updated_at").toLocalDateTime());
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
	 내 로그 리스트 조회 (요약용)
	 */
	public List<MyLogListDto> findMyLogList(String userId) {
		
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    List<MyLogListDto> list = new ArrayList<>();

	    String sql = """
	            SELECT 
	                l.l_id,
	                l.l_result_status,
	                l.l_title,
	                l.l_created_at,
	                c.c_name
	            FROM log l
	            JOIN category c ON l.c_id = c.c_id
	            WHERE l.u_id = ?
	            ORDER BY l.l_created_at DESC
	            """;

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);

	        pstmt.setString(1, userId);

	        rs = pstmt.executeQuery();

	        while (rs.next()) {

	            MyLogListDto dto = new MyLogListDto();

	            dto.setId(rs.getLong("l_id"));
	            dto.setResultStatus(rs.getString("l_result_status"));
	            dto.setTitle(rs.getString("l_title"));
	            dto.setCategoryName(rs.getString("c_name"));

	            if (rs.getTimestamp("l_created_at") != null) {
	                dto.setCreatedAt(
	                    rs.getTimestamp("l_created_at").toLocalDateTime()
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
	
	/*
	 로그 상세 조회
	 */
	public LogDto findById(Long logId) {

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    LogDto dto = null;

	    String sql = """
	            SELECT *
	            FROM log
	            WHERE l_id = ?
	            """;

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);

	        pstmt.setLong(1, logId);

	        rs = pstmt.executeQuery();

	        if (rs.next()) {

	            dto = new LogDto();

	            dto.setId(rs.getLong("l_id"));
	            dto.setUserId(rs.getString("u_id"));
	            dto.setTitle(rs.getString("l_title"));
	            dto.setContentTitle(rs.getString("l_content_title"));
	            dto.setContentUrl(rs.getString("l_content_url"));
	            dto.setPlatformId(rs.getLong("pf_id"));
	            dto.setCategoryId(rs.getLong("c_id"));
	            dto.setTryContent(rs.getString("l_try_content"));
	            dto.setResultStatus(rs.getString("l_result_status"));
	            dto.setFailResult(rs.getString("l_fail_result"));
	            dto.setFailReason(rs.getString("l_failure_reason"));
	            dto.setIsPublic(rs.getBoolean("l_is_public"));
	            dto.setIsDraft(rs.getBoolean("l_is_draft"));
	            dto.setViewCount(rs.getLong("l_view_count"));

	            if (rs.getTimestamp("l_created_at") != null) {
	                dto.setCreatedAt(
	                    rs.getTimestamp("l_created_at").toLocalDateTime()
	                );
	            }

	            if (rs.getTimestamp("l_updated_at") != null) {
	                dto.setUpdatedAt(
	                    rs.getTimestamp("l_updated_at").toLocalDateTime()
	                );
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }

	    return dto;
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
	                l_created_at
	            FROM log
	            WHERE l_is_public = 1
	              AND l_is_draft = 0
	            ORDER BY l_created_at DESC
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

	            if (rs.getTimestamp("l_created_at") != null) {
	                dto.setCreatedAt(
	                        rs.getTimestamp("l_created_at").toLocalDateTime()
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
}
