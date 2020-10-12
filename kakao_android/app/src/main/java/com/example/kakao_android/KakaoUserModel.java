package com.example.kakao_android;

import java.io.Serializable;

public class KakaoUserModel implements Serializable {
    private String nickname;
    private String email;
    private String gender;
    private String birthday;

    public KakaoUserModel(String nickname, String email, String gender, String birthday) {
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
