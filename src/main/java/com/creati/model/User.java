package com.creati.model;

import java.time.LocalDate;

public class User {
    private final String id;
    private final String password;
    private final String nickname;
    private final String profileResPath;
    private final String phone;
    private final String email;
    private final LocalDate birth;
    private final String platform;

    // 기존 코드와 호환되는 생성자 (건드리지 않아도 됨)
    public User(String id, String password, String nickname, String profileResPath) {
        this(id, password, nickname, profileResPath, null, null, null, null);
    }

    // 새 생성자
    public User(String id, String password, String nickname, String profileResPath,
                String phone, String email, LocalDate birth, String platform) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.profileResPath = profileResPath;
        this.phone = phone;
        this.email = email;
        this.birth = birth;
        this.platform = platform;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileResPath() { return profileResPath; }
    public String getPhone() { return phone == null ? "" : phone; }
    public String getEmail() { return email == null ? "" : email; }
    public LocalDate getBirth() { return birth; }
    public String getPlatform() { return platform == null ? "" : platform; }
}