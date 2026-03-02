package com.creati.dao;

import com.creati.database.DBConnectionMgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ai_analysis 테이블 DAO
 *
 * 스키마:
 *   a_id        VARCHAR(50)  PK
 *   l_id        BIGINT       FK → log.l_id
 *   a_type      VARCHAR(20)  (CAUSE | RETRO | RETRY)
 *   a_title     VARCHAR(255)
 *   a_content   TEXT
 *   a_created_at DATETIME    DEFAULT CURRENT_TIMESTAMP
 */
public class AiAnalysisDao {

    private final DBConnectionMgr pool;

    public AiAnalysisDao() {
        this.pool = DBConnectionMgr.getInstance();
    }

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────
    public boolean insert(String aId, long lId, String aType, String aTitle, String aContent) {
        String sql = "INSERT INTO ai_analysis (a_id, l_id, a_type, a_title, a_content) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, aId);
            ps.setLong(2, lId);
            ps.setString(3, aType);
            ps.setString(4, aTitle);
            ps.setString(5, aContent);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(conn, ps);
        }
    }

    // ─────────────────────────────────────────────
    // SELECT: by l_id (최신순)
    // ─────────────────────────────────────────────
    public List<AiAnalysisRow> findByLogId(long lId) {
        String sql = "SELECT a_id, l_id, a_type, a_title, a_content, a_created_at " +
                     "FROM ai_analysis WHERE l_id = ? ORDER BY a_created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AiAnalysisRow> list = new ArrayList<>();
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, lId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
        return list;
    }

    // ─────────────────────────────────────────────
    // 존재 여부
    // ─────────────────────────────────────────────
    public boolean existsByLogIdAndType(long lId, String aType) {
        String sql = "SELECT 1 FROM ai_analysis WHERE l_id = ? AND a_type = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, lId);
            ps.setString(2, aType);
            rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(conn, ps, rs);
        }
    }

    // ─────────────────────────────────────────────
    // row mapper
    // ─────────────────────────────────────────────
    private AiAnalysisRow mapRow(ResultSet rs) throws Exception {
        String  aId     = rs.getString("a_id");
        long    lId     = rs.getLong("l_id");
        String  aType   = rs.getString("a_type");
        String  aTitle  = rs.getString("a_title");
        String  aContent = rs.getString("a_content");
        LocalDate createdAt = LocalDate.now();
        Timestamp ts = rs.getTimestamp("a_created_at");
        if (ts != null) createdAt = ts.toLocalDateTime().toLocalDate();
        return new AiAnalysisRow(aId, lId, aType, aTitle, aContent, createdAt);
    }

    // ─────────────────────────────────────────────
    // 내부 DTO
    // ─────────────────────────────────────────────
    public static class AiAnalysisRow {
        public final String   aId;
        public final long     lId;
        public final String   aType;
        public final String   aTitle;
        public final String   aContent;
        public final LocalDate createdAt;

        public AiAnalysisRow(String aId, long lId, String aType,
                             String aTitle, String aContent, LocalDate createdAt) {
            this.aId      = aId;
            this.lId      = lId;
            this.aType    = aType;
            this.aTitle   = aTitle;
            this.aContent = aContent;
            this.createdAt = createdAt;
        }
    }
}