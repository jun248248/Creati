package com.creati.dao;

import com.creati.database.DBConnectionMgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReactionDao {

    private DBConnectionMgr dbMgr;

    public ReactionDao() {
        dbMgr = DBConnectionMgr.getInstance();
    }

    // -------------------------------
    // 1. 사용자의 기존 리액션 조회
    // -------------------------------
    public Long getUserReaction(long logId, String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT rt_id FROM log_reaction WHERE l_id = ? AND u_id = ?";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, logId);
            pstmt.setString(2, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("rt_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt, rs);
        }

        return null;
    }

    // -------------------------------
    // 2. 리액션 등록 (INSERT)
    // -------------------------------
    public void insertReaction(long logId, long rtId, String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO log_reaction (l_id, rt_id, u_id) VALUES (?, ?, ?)";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, logId);
            pstmt.setLong(2, rtId);
            pstmt.setString(3, userId);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt);
        }
    }

    // -------------------------------
    // 3. 리액션 변경 (UPDATE)
    // -------------------------------
    public void updateReaction(long logId, long rtId, String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE log_reaction SET rt_id = ? WHERE l_id = ? AND u_id = ?";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, rtId);
            pstmt.setLong(2, logId);
            pstmt.setString(3, userId);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt);
        }
    }

    // -------------------------------
    // 4. 리액션 삭제 (선택 기능)
    // -------------------------------
    public void deleteReaction(long logId, String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM log_reaction WHERE l_id = ? AND u_id = ?";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, logId);
            pstmt.setString(2, userId);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt);
        }
    }

    // -------------------------------
    // 5. 특정 리액션 개수
    // -------------------------------
    public int countByReaction(long logId, long rtId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COUNT(*) FROM log_reaction WHERE l_id = ? AND rt_id = ?";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, logId);
            pstmt.setLong(2, rtId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt, rs);
        }

        return 0;
    }

    // -------------------------------
    // 6. 전체 리액션 개수
    // -------------------------------
    public int countTotal(long logId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COUNT(*) FROM log_reaction WHERE l_id = ?";

        try {
            conn = dbMgr.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, logId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbMgr.freeConnection(conn, pstmt, rs);
        }

        return 0;
    }
}