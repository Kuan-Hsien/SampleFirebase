package com.kuanhsien.samplefirebase;


import java.util.HashMap;
import java.util.Map;

public class User {

    private String mUserId;
    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String mFriends;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String username, String email, String password, String friends) {
        this.mUserId = userId;
        this.mUsername = username;
        this.mEmail = email;
        this.mPassword = password;
        this.mFriends = friends;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", mUserId);
        result.put("username", mUsername);
        result.put("email", mEmail);
        result.put("password", mPassword);
        result.put("friends", mFriends);

        return result;
    }

}