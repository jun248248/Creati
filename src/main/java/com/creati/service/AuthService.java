package com.creati.service;

import com.creati.dao.UserDao;
import com.creati.dto.UserDto;
import com.creati.model.User;

import java.time.LocalDate;
import java.util.List;

public class AuthService {

    private final UserDao userDao = new UserDao();

    private static final AuthService INSTANCE = new AuthService();

    public static AuthService getInstance() {
        return INSTANCE;
    }

    private AuthService() {}

    // =========================
    // 로그인
    // =========================
    public User login(String id, String pw) {

        UserDto dto = userDao.login(id, pw);

        if (dto == null) return null;

        User user = new User();

        user.setId(dto.getId());
        user.setPassword(pw); // 나중에 암호화 적용 예정
        user.setNickname(dto.getName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setBirth(dto.getBirth());
        user.setPlatform(dto.getPlatform());


        // TODO: 관심분야 조회해서 넣기
        // user.setInterests(userDao.getUserInterests(id));

        return user;
    }

    // =========================
    // 아이디 중복 확인
    // =========================
    public boolean isIdAvailable(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return !userDao.isDuplicateId(id);
    }


    // =========================
    // 회원가입
    // =========================
    public boolean signup(
            String id,
            String pw,
            String nickname,
            String phone,
            String email,
            LocalDate birth,
            String platform,
            List<Long> interestIds) {

        if (id == null || pw == null || nickname == null) return false;

        id = id.trim();
        pw = pw.trim();
        nickname = nickname.trim();

        if (id.isEmpty() || pw.isEmpty() || nickname.isEmpty()) return false;

        // 중복 체크
        if (userDao.isDuplicateId(id)) return false;

        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setPwHash(pw); // TODO: BCrypt 적용
        dto.setName(nickname);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setBirth(birth);
        dto.setPlatform(platform);

        return userDao.insertUser(dto, interestIds);
    }

    // =========================
    // 회원정보 수정
    // =========================
    public boolean updateUserInfo(User user, List<Long> interestIds) {

        boolean userUpdated = userDao.updateUser(user);
        boolean interestUpdated = userDao.updateUserInterests(user.getId(), interestIds);

        return userUpdated && interestUpdated;
    }

    // =========================
    // 비밀번호 변경
    // =========================
    public boolean changePassword(String userId, String currentPw, String newPw) {
        return userDao.updatePassword(userId, currentPw, newPw);
    }
}