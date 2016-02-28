package com.gautier_lefebvre.epitechmessengerapp.entity;

public class Message {
    public final User sender;
    public final String content;
    public final String date;

    public Message(User sender, String content, String date) {
        this.sender = sender;
        this.content = content;
        this.date = date;
    }
}
