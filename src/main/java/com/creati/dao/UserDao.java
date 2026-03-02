package com.creati.dao;

import com.creati.dto.UserDto;
import com.creati.model.User;
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
    public UserDto login(String userId, String password) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM users WHERE u_id = ?";

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {

            	String storedPw = rs.getString("u_pw_hash");
            	
            	if (!storedPw.equals(password)) {
                    return null;
                }
            	UserDto user = new UserDto();

                user.setId(rs.getString("u_id"));
                user.setName(rs.getString("u_name"));
                user.setPhone(rs.getString("u_phone"));
                user.setEmail(rs.getString("u_email"));

                if (rs.getDate("u_birth") != null) {
                    user.setBirth(rs.getDate("u_birth").toLocalDate());
                } else {
                    user.setBirth(null);
                }

                user.setPlatform(rs.getString("u_platform"));

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
            String userSql = "INSERT INTO users " +
                    "(u_id, u_pw_hash, u_name, u_phone, u_email, u_birth, u_platform) " +
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

            userStmt.setString(7, user.getPlatform());

            userStmt.executeUpdate();

            // 관심분야 insert (최대 3개)
            if (interestIds.size() > 3) {
                throw new RuntimeException("관심분야는 최대 3개까지 가능합니다.");
            }

            String interestSql = "INSERT INTO user_interest (u_id, i_id) VALUES (?, ?)";
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

        String sql = "SELECT u_id FROM users WHERE u_id = ?";

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
    
    /*
     * 회원정보 수정
     */
    public boolean updateUser(User user) {

        String sql = "UPDATE users SET u_name=?, u_birth=?, u_email=?, u_platform=? WHERE u_id=?";

        try (Connection conn = pool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getNickname());

            // 🔥 LocalDate → SQL Date
            if (user.getBirth() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(user.getBirth()));
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }

            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPlatform());
            pstmt.setString(5, user.getId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    /*
     * 유저 관심분야 수정
     */
    public boolean updateUserInterests(String userId, List<Long> interestIds) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);

            // 1. 기존 삭제
            String deleteSql = "DELETE FROM user_interest WHERE u_id=?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setString(1, userId);
            pstmt.executeUpdate();

            // 2. 새로 insert
            String insertSql = "INSERT INTO user_interest (u_id, i_id) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertSql);

            for (Long id : interestIds) {
                pstmt.setString(1, userId);
                pstmt.setLong(2, id);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (Exception ignored) {}
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            pool.freeConnection(conn, pstmt);
        }

        return false;
    }
    
    /*
     * 비밀번호 변경
     */
    public boolean updatePassword(String userId, String currentPw, String newPw) {

    	String checkSql = "SELECT u_pw_hash FROM users WHERE u_id=?";
    	String updateSql = "UPDATE users SET u_pw_hash=? WHERE u_id=?";

        try (Connection conn = pool.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String dbPw = rs.getString("u_pw_hash");

                if (!dbPw.equals(currentPw)) {
                    return false; // 현재 비밀번호 불일치
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPw);
                updateStmt.setString(2, userId);

                return updateStmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}