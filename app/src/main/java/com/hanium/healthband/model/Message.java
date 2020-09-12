package com.hanium.healthband.model;

public class Message {
    private String username;
    private String content;
    private double lat;
    private double lang;

    public Message(String username, String content) {
        this.username = username;
        this.content = content;
    }

    public Message(String username, String content, double lat, double lang) {
        this.username = username;
        this.content = content;
        this.lat = lat;
        this.lang = lang;
    }
}
