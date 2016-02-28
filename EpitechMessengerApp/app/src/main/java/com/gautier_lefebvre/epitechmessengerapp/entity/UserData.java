package com.gautier_lefebvre.epitechmessengerapp.entity;

public class UserData {
    public int userId;
    public String nickname;

    @Override
    public String toString() {
        return this.nickname;
    }
}
