package com.creati.service;

import com.creati.dao.UserDao;
import com.creati.dto.UserDto;
import com.creati.model.User;

import java.util.*;

public class AuthService {

	private UserDao userDao = new UserDao();
	
	private static final AuthService INSTANCE = new AuthService();

	public static AuthService getInstance() {
		return INSTANCE;
	}

	// DB
	private final Map<String, User> users = new HashMap<>();

	private static final String[] PROFILE_RES = new String[] { "/images/profile/profile_red.png",
			"/images/profile/profile_orange.png", "/images/profile/profile_yellow.png",
			"/images/profile/profile_green.png", "/images/profile/profile_blue.png",
			"/images/profile/profile_purple.png", "/images/profile/profile_gray.png" };

	private final Random random = new Random();

	private AuthService() {
		
		ensureDefaultUser();
	}

	private void ensureDefaultUser() {
		if (!users.containsKey("aaa")) {
			String profile = pickRandomProfile();
			users.put("aaa", new User("aaa", "1234", "오늘도한걸음", profile));
		}
	}

	private String pickRandomProfile() {
	    if (PROFILE_RES == null || PROFILE_RES.length == 0) {
	        return "/images/profile/default_profile.png";
	    }
	    return PROFILE_RES[random.nextInt(PROFILE_RES.length)];
	}

	public User login(String id, String pw) {

	    UserDto dto = userDao.login(id, pw);

	    if (dto == null) {
	        return null;
	    }

	    return new User(
	            dto.getId(),
	            pw,                     
	            dto.getName(),           
	            null
	    );
	}

	public boolean isDuplicateId(String id) {
		return id != null && users.containsKey(id);
	}

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

	public User findUser(String id) {
		return users.get(id);
	}
}
