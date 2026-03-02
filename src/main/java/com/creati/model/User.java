package com.creati.model;

import java.time.LocalDate;
import java.util.List;

public class User {

    private String id;
    private String password;
    private String nickname;

    private String phone;
    private String email;
    private LocalDate birth;
    private String platform;

    private List<String> interests;

    private String profileResPath;

    // 기본 생성자 (필수)
    public User() {}

    // 로그인용 생성자
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
    }

    // getter / setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirth() { return birth; }
    public void setBirth(LocalDate birth) { this.birth = birth; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public String getProfileResPath() { return profileResPath; }
    public void setProfileResPath(String profileResPath) { this.profileResPath = profileResPath; }
}