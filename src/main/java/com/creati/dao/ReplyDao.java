package com.creati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.creati.database.DBConnectionMgr;
import com.creati.dto.ReplyDto;

public class ReplyDao {
	
	private DBConnectionMgr pool;

	public ReplyDao() {
		pool = DBConnectionMgr.getInstance();
	}

	public List<ReplyDto> getRepliesByLogId(long logId) {

	    List<ReplyDto> list = new ArrayList<>();

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "SELECT r.*, u.u_name FROM reply r JOIN users u ON r.u_id = u.u_id WHERE r.l_id = ? ORDER BY r.r_created_at ASC";

	    try {
	        conn = pool.getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setLong(1, logId);

	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            ReplyDto dto = new ReplyDto();
	            dto.setrId(rs.getLong("r_id"));
	            dto.setlId(rs.getLong("l_id"));
	            dto.setUserId(rs.getString("u_id"));
	            dto.setNickname(rs.getString("u_name"));
	            dto.setContent(rs.getString("r_content"));
	            dto.setCreatedAt(rs.getTimestamp("r_created_at").toLocalDateTime());

	            list.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(conn, pstmt, rs);
	    }

	    return list;
	}
	
	// ── 댓글 저장 (신규 추가) ──────────────────────────────────
    public boolean insertReply(long logId, String userId, String content) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO reply (l_id, u_id, r_content, r_created_at) VALUES (?, ?, ?, ?)";

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, logId);
            pstmt.setString(2, userId);
            pstmt.setString(3, content);
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(conn, pstmt);
        }
    }
}
