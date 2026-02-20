package com.creati.service;

import com.creati.model.User;

import java.util.*;

public class AuthService {

	// 싱글톤 인스턴스
	private static final AuthService INSTANCE = new AuthService();

	public static AuthService getInstance() {
		return INSTANCE;
	}

		// TODO(DB) 사용자 테이블 연동 포인트
	// TODO(REMOVE) 메모리 저장소

	private final Map<String, User> users = new HashMap<>();

	// 프로필 리소스
	private static final String[] PROFILE_RES = new String[] { "/images/profile/profile_red.png",
			"/images/profile/profile_orange.png", "/images/profile/profile_yellow.png",
			"/images/profile/profile_green.png", "/images/profile/profile_blue.png",
			"/images/profile/profile_purple.png", "/images/profile/profile_gray.png" };

	private final Random random = new Random();

	private AuthService() {
		// 기본 계정(aaa/1234): 랜덤 프로필 하나
		ensureDefaultUser();
	}

	private void ensureDefaultUser() {
		if (!users.containsKey("aaa")) {
			String profile = pickRandomProfile();
			users.put("aaa", new User("aaa", "1234", "aaa", profile));
		}
	}

	private String pickRandomProfile() {
	    if (PROFILE_RES == null || PROFILE_RES.length == 0) {
	        return "/images/profile/default_profile.png";
	    }
	    return PROFILE_RES[random.nextInt(PROFILE_RES.length)];
	}

	// 로그인: 성공하면 User 반환, 실패면 null
	public User login(String id, String pwd) {
		if (id == null || pwd == null)
			return null;
		User u = users.get(id);
		if (u == null)
			return null;
		return u.getPassword().equals(pwd) ? u : null;
	}

	public boolean isDuplicateId(String id) {
		return id != null && users.containsKey(id);
	}

	// 회원가입: 랜덤 프로필 자동 지정
	public boolean signup(String id, String pw, String nickname) {
		if (id == null || pw == null || nickname == null)
			return false;
		id = id.trim();
		pw = pw.trim();
		nickname = nickname.trim();

		if (id.isEmpty() || pw.isEmpty() || nickname.isEmpty())
			return false;
		if (users.containsKey(id))
			return false;

		String profile = pickRandomProfile();
		users.put(id, new User(id, pw, nickname, profile));
		return true;
	}

	// 필요하면 나중에 사용(프로필 조회)
	public User findUser(String id) {
		return users.get(id);
	}
}
