package com.creati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.creati.database.DBConnectionMgr;
import com.creati.dto.LogDto;

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
	 * 공개 여부에 따라 게시글 목록 조회
	 */
	public List<LogDto> findByUserIdAndPublic(String userId, boolean isPublic) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<LogDto> list = new ArrayList<>();

		String sql = """
				SELECT *
				FROM log
				WHERE u_id = ?
				  AND l_is_public = ?
				  AND l_is_draft = 0
				ORDER BY l_created_at DESC
				""";

		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, userId);
			pstmt.setBoolean(2, isPublic);

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
}
