package com.creati.dao;

import com.creati.dto.UserDto;
import com.creati.database.DBConnectionMgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

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
     */
    public boolean insertUser(UserDto user) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO user " +
                     "(u_id, u_pw_hash, u_name, u_phone, u_email, u_birth, pf_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conn = pool.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getPwHash());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getEmail());

            if (user.getBirth() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(user.getBirth()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }

            pstmt.setLong(7, user.getPlatformId());

            int result = pstmt.executeUpdate();

            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(conn, pstmt);
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
