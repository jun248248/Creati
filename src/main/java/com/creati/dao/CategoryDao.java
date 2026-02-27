package com.creati.dao;

import com.creati.database.DBConnectionMgr;

import java.sql.*;

public class CategoryDao {
    private final DBConnectionMgr pool;

    public CategoryDao() {
        this.pool = DBConnectionMgr.getInstance();
    }

    /** 성공: c_id 반환, 실패: -1 */
    public long ensureCategoryId(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("[CategoryDao] categoryName is null/blank");
            return -1;
        }
        String name = categoryName.trim();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            // 1) 조회
            ps = conn.prepareStatement("SELECT c_id FROM category WHERE c_name = ?");
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
                    "INSERT INTO category (c_name) VALUES (?)",
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
            // 중복 삽입(경쟁) -> 다시 조회해서 회수
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            Long id = findCategoryIdByNameNoThrow(name);
            return id != null ? id : -1;

        } catch (Exception e) {
            System.out.println("[CategoryDao] ensureCategoryId failed: " + e.getMessage());
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            return -1;

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            pool.freeConnection(conn, ps, rs);
        }
    }

    private Long findCategoryIdByNameNoThrow(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT c_id FROM category WHERE c_name = ?");
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