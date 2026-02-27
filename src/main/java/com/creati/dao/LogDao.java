package com.creati.dao;

import com.creati.database.DBConnectionMgr;
import com.creati.model.LogPost;
import com.creati.model.LogStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDao {
    private DBConnectionMgr pool;

    public LogDao() {
        pool = DBConnectionMgr.getInstance();
    }

    public List<LogPost> selectAllLogs() {
        List<LogPost> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        String sql = "SELECT l_id, l_title, l_result_status, created_at, l_process, i_id FROM log ORDER BY created_at DESC";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToLogPost(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return list;
    }

    public LogPost selectLogById(String id) {
        long l_id;
        try {
            l_id = Long.parseLong(id);
        } catch (Exception e) {
            return null;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT l_id, l_title, l_result_status, created_at, l_process, i_id FROM log WHERE l_id = ?";

        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, l_id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLogPost(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return null;
    }

    private LogPost mapResultSetToLogPost(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("l_result_status"); // SUCCESS, FAIL, ONGOING 등
        
        LogStatus status = LogStatus.IN_PROGRESS;
        if (statusStr != null) {
            status = switch (statusStr.toUpperCase()) {
                case "SUCCESS" -> LogStatus.DONE;
                case "FAIL" -> LogStatus.NEEDS_IMPROVEMENT;
                default -> LogStatus.IN_PROGRESS;
            };
        }
        
        return new LogPost(
            String.valueOf(rs.getLong("l_id")),
            rs.getString("i_id"), // i_id를 분야(field)로 매핑
            null,
            status,
            rs.getString("l_title"),
            rs.getTimestamp("created_at").toLocalDateTime().toLocalDate(), // [수정]
            true,
            rs.getString("l_process"), // l_process를 내용(whatIDid)으로 매핑
            null, null, null, null, null
        );
    }
    
    /**
     * 로그를 저장하거나 기존 로그를 업데이트합니다.
     */
 // LogDao.java의 upsertLog 메서드 보완
    public boolean upsertLog(LogPost vo) {
        Connection con = null;
        PreparedStatement pstmt = null;
        
        // ON DUPLICATE KEY UPDATE를 사용하여 QNA 데이터도 처리
        String sql = "INSERT INTO log (l_id, l_title, l_result_status, created_at, l_process, i_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE l_title=?, l_process=?, i_id=?";

        try {
            con = pool.getConnection(); //
            pstmt = con.prepareStatement(sql);

            // ID 처리: qna_ 등의 접두사가 있다면 숫자만 추출하거나 현재 시간을 사용
            long lid;
            try {
                // 숫자만 있는 경우 파싱, 문자열이 섞인 경우 타임스탬프 활용
                String cleanId = vo.id.replaceAll("[^0-9]", ""); 
                lid = cleanId.isEmpty() ? System.currentTimeMillis() : Long.parseLong(cleanId);
            } catch (NumberFormatException e) {
                lid = System.currentTimeMillis();
            }

            pstmt.setLong(1, lid);
            pstmt.setString(2, vo.title);
            pstmt.setString(3, (vo.status != null) ? vo.status.name() : "ONGOING");
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(vo.createdAt.atStartOfDay()));
            // QNA의 경우 whatIDid(질문내용)가 l_process 컬럼에 매핑되도록 설정
            pstmt.setString(5, vo.whatIDid); 
            pstmt.setString(6, vo.field);

            // UPDATE 파트
            pstmt.setString(7, vo.title);
            pstmt.setString(8, vo.whatIDid);
            pstmt.setString(9, vo.field);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.freeConnection(con, pstmt); //
        }
    }
}