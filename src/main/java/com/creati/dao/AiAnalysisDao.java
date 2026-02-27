package com.creati.dao;

import com.creati.database.DBConnectionMgr;
import com.creati.ui.main.AiAnalysisRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiAnalysisDao {
    private DBConnectionMgr pool;

    public AiAnalysisDao() {
        pool = DBConnectionMgr.getInstance();
    }

    /**
     * AI 분석 결과를 DB에 저장합니다.
     */
    public boolean insertAnalysis(long logId, String type, String title, String content) {
        Connection con = null;
        PreparedStatement pstmt = null;
        // 이미지 image_2a182a.png에 정의된 테이블 구조와 컬럼명(a_id, l_id, a_type, a_title, a_content, a_created_at)을 정확히 사용합니다.
        String sql = "INSERT INTO ai_analysis (a_id, l_id, a_type, a_title, a_content, a_created_at) VALUES (?, ?, ?, ?, ?, NOW())";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            
            // a_id: VARCHAR(50)이므로 UUID를 생성하여 넣습니다.
            pstmt.setString(1, UUID.randomUUID().toString());
            // l_id: BIGINT이므로 숫자로 넣습니다.
            pstmt.setLong(2, logId);
            pstmt.setString(3, type);
            pstmt.setString(4, title);
            pstmt.setString(5, content);

            System.out.println("AI 분석 결과 DB 저장 시도: LogId=" + logId); // 디버깅용
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    /**
     * 특정 로그의 모든 분석 기록을 가져옵니다.
     */
    public List<AiAnalysisRecord> findByLogId(String logId) {
        List<AiAnalysisRecord> list = new ArrayList<>();
        long l_id;
        try {
            // 숫자만 추출하여 파싱 (NumberFormatException 방지)
            l_id = Long.parseLong(logId.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return list;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM ai_analysis WHERE l_id = ? ORDER BY a_created_at DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, l_id);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new AiAnalysisRecord(
                    rs.getString("a_id"),
                    String.valueOf(rs.getLong("l_id")),
                    AiAnalysisRecord.Type.valueOf(rs.getString("a_type")),
                    rs.getString("a_title"),
                    rs.getTimestamp("a_created_at").toLocalDateTime().toLocalDate(),
                    rs.getString("a_content")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }
}