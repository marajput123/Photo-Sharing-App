package com.example.hw07;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

public class Photo implements Serializable {
    private ArrayList<String> likes;
    private String photoUrl;
    private String id;
    private String userId;
    private Uri photoUri;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public Photo(ArrayList<String> likes, String photoUrl, String id, String userId) {
        this.likes = likes;
        this.photoUrl = photoUrl;
        this.id = id;
        this.userId = userId;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
