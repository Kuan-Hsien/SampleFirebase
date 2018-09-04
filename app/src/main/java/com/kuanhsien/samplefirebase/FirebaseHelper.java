package com.kuanhsien.samplefirebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static com.kuanhsien.samplefirebase.MainActivity.mStrUserEmail;
import static com.kuanhsien.samplefirebase.MainActivity.mStrUserId;

public class FirebaseHelper {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


//    public void writeNewUser(String userId, String name, String email) {
//        User user = new User(name, email);
//
//        mDatabase.child("users").child(userId).setValue(user);
//    }


//    public void writeNewPost(String userId, String username, String title, String body) {
//        // Create new post at /user-posts/$userid/$postid and at
//        // /posts/$postid simultaneously
//        String key = mDatabase.child("posts").push().getKey();
//
//        Post post = new Post(userId, username, title, body);
//
//        Map<String, Object> postValues = post.toMap();
//
//
//
//        Map<String, Object> childUpdates = new HashMap<>();
//
//        childUpdates.put("/posts/" + key, postValues);
//        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
//
//        mDatabase.updateChildren(childUpdates);
//    }


    public void writeNewArticle(String userId, String title, String content, String tag, String createdTime) {

        String key = mDatabase.child("article").push().getKey();

        Article article = new Article(key, mStrUserEmail, title, content, tag, createdTime);

        Map<String, Object> articleValues = article.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/article/" + key, articleValues);


        mDatabase.updateChildren(childUpdates);
    }


    public void writeNewUser(String username, String email, String password) {

        String key = mDatabase.child("user").push().getKey();

        User user = new User(key, username, email, password, "");

        mStrUserEmail = email;
        mStrUserId = key;

        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/user/" + key, userValues);

        mDatabase.updateChildren(childUpdates);
    }
}
