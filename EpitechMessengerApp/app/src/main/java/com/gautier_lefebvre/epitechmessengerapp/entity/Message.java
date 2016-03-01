package com.gautier_lefebvre.epitechmessengerapp.entity;

public class Message {
    public final User sender;
    public final String content;
    public final String date;
    public boolean isRead;

    public Message(User sender, String content, String date, boolean r) {
        this.sender = sender;
        this.content = content;
        this.date = date;
        this.isRead = r;
    }
}
