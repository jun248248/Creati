package com.creati.dao;

import com.creati.dto.UserDto;
import com.creati.database.DBConnectionMgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class UserDao {

    private DBConnectionMgr pool;

    public UserDao() {
        pool = DBConnectionMgr.getInstance();
    }

    /*
     로그인
     */
    public UserDto login(String userId, String passwordHash) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM user WHERE u_id = ? AND u_pw_hash = ?";

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, userId);
            pstmt.setString(2, passwordHash);

            rs = pstmt.executeQuery();

            if (rs.next()) {

            	UserDto user = new UserDto();

                user.setId(rs.getString("u_id"));
                user.setPwHash(rs.getString("u_pw_hash"));
                user.setName(rs.getString("u_name"));
                user.setPhone(rs.getString("u_phone"));
                user.setEmail(rs.getString("u_email"));

                if (rs.getDate("u_birth") != null) {
                    user.setBirth(rs.getDate("u_birth").toLocalDate());
                } else {
                    user.setBirth(null);
                }

                user.setPlatformId(rs.getLong("pf_id"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt, rs);
        }

        return null;
    }
    
    /*
     회원가입 - 관심분야 추가
     트랜잭션 이유 : users 테이블에 정보 저장과 interest 테이블에 정보 저장이 동시에 되야하기 때문에
     */
    public boolean insertUser(UserDto user, List<Long> interestIds) {

    	Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement interestStmt = null;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false); //트랜잭션 시작

            // user 테이블 insert
            String userSql = "INSERT INTO user " +
                    "(u_id, u_pw_hash, u_name, u_phone, u_email, u_birth, pf_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            userStmt = conn.prepareStatement(userSql);

            userStmt.setString(1, user.getId());
            userStmt.setString(2, user.getPwHash());
            userStmt.setString(3, user.getName());
            userStmt.setString(4, user.getPhone());
            userStmt.setString(5, user.getEmail());

            if (user.getBirth() != null) {
                userStmt.setDate(6, java.sql.Date.valueOf(user.getBirth()));
            } else {
                userStmt.setNull(6, java.sql.Types.DATE);
            }

            userStmt.setLong(7, user.getPlatformId());

            userStmt.executeUpdate();

            // 관심분야 insert (최대 3개)
            if (interestIds.size() > 3) {
                throw new RuntimeException("관심분야는 최대 3개까지 가능합니다.");
            }

            String interestSql = "INSERT INTO user_interest (u_id, interest_id) VALUES (?, ?)";
            interestStmt = conn.prepareStatement(interestSql);

            for (Long interestId : interestIds) {
                interestStmt.setString(1, user.getId());
                interestStmt.setLong(2, interestId);
                interestStmt.executeUpdate();
            }

            conn.commit(); // 성공 시 커밋
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback(); // 실패 시 롤백
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (Exception ignored) {}

            pool.freeConnection(conn, userStmt);
            if (interestStmt != null) {
                try { interestStmt.close(); } catch (Exception ignored) {}
            }
        }

        return false;
    }
    
    /*
     아이디 중복확인
     중복 true
     중복 아니면 false
     */
    public boolean isDuplicateId(String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT u_id FROM user WHERE u_id = ?";

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);

            rs = pstmt.executeQuery();

            // 커서가 이동할 행이 있으면 = 결과가 하나라도 있으면 중복 true 리턴
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt, rs);
        }

        return false;
    }
}
