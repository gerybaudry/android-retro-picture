package com.gautier_lefebvre.epitechmessengerapp.entity;

import java.util.ArrayList;

public class Contact extends User {
    public final ArrayList<Message> conversationHistory = new ArrayList<>();
    public boolean added;

    public Contact(int id, String nickname, boolean added) {
        super(id, nickname);
        this.added = added;
    }

    @Override
    public String toString() {
        return this.nickname;
    }
}
