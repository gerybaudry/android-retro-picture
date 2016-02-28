package com.gautier_lefebvre.epitechmessengerapp.entity;

public class User {
    public final int userId;
    public String nickname;

    public User(int id, String nickname) {
        this.userId = id;
        this.nickname = nickname;
    }
}
