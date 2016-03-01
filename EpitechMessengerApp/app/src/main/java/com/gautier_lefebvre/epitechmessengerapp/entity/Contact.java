package com.gautier_lefebvre.epitechmessengerapp.entity;

import java.util.ArrayList;

public class Contact extends User {
    public final ArrayList<Message> conversationHistory = new ArrayList<>();
    public boolean added;

    public Contact(int id, String nickname, boolean added) {
        super(id, nickname);
        this.added = added;
    }

    public int getUnreadMessagesNb() {
        int unread = 0;
        for (Message m : this.conversationHistory) {
            if (!m.isRead) {
                unread++;
            }
        }
        return unread;
    }

    public void setAllMessagesRead() {
        for (Message m : this.conversationHistory) {
            m.isRead = true;
        }
    }

    @Override
    public String toString() {
        return this.nickname;
    }
}
