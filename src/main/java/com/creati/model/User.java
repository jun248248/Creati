package com.creati.model;

public class User {
    private final String id;
    private final String password;
    private final String nickname;
    private final String profileResPath; // 예: "/images/profile/profile_red.png"

    public User(String id, String password, String nickname, String profileResPath) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.profileResPath = profileResPath;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileResPath() { return profileResPath; }
}
