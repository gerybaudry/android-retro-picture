package com.gautier_lefebvre.epitechmessengerapp.entity;

import java.util.ArrayList;

public class CurrentUser extends User {
    public final String authToken;
    public final String email;
    public final ArrayList<Contact> contacts = new ArrayList<>();

    public CurrentUser(int id, String nickname, String email, String authToken) {
        super(id, nickname);
        this.authToken = authToken;
        this.email = email;
    }
}
