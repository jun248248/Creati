package com.creati.dao;

import com.creati.database.DBConnectionMgr;

import java.sql.*;

public class InterestDao {
    private final DBConnectionMgr pool;

    public InterestDao() {
        this.pool = DBConnectionMgr.getInstance();
    }

    /** 관심분야명으로 i_id 보장(없으면 생성)해서 반환. 실패 시 -1 */
    public long ensureInterestId(String interestName) {
        if (interestName == null || interestName.trim().isEmpty()) return -1;
        String name = interestName.trim();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            // 1) 조회
            ps = conn.prepareStatement("SELECT i_id FROM interest WHERE i_name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                conn.commit();
                return id;
            }

            // 2) 없으면 생성
            closeQuietly(rs);
            closeQuietly(ps);

            ps = conn.prepareStatement(
                    "INSERT INTO interest (i_name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                conn.rollback();
                return -1;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long newId = rs.getLong(1);
                conn.commit();
                return newId;
            }

            conn.rollback();
            return -1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // 중복 insert 경쟁 -> 다시 조회
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            Long id = findInterestIdByNameNoThrow(name);
            return id != null ? id : -1;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            return -1;

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            pool.freeConnection(conn, ps, rs);
        }
    }

    private Long findInterestIdByNameNoThrow(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT i_id FROM interest WHERE i_name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    private void closeQuietly(AutoCloseable c) {
        try { if (c != null) c.close(); } catch (Exception ignore) {}
    }
}