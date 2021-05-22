package com.example.hw07;

public class Comment {
    private String text;
    private String name;
    private String userId;
    private String id;

    public Comment(String text, String name, String userId, String id) {
        this.text = text;
        this.name = name;
        this.userId = userId;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
