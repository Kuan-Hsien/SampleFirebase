package com.kuanhsien.samplefirebase;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String mStrUserId;
    public static String mStrUserEmail;

    public static final String TAG = "KEN_FIRE";
    public static final String MSG = "MainActivity: ";
    public static final String DATABASE_NAME = "myfancyproject-awesome";

    private String[] mStrArticleTagId = {"ALL", "BEAUTY", "GOSSIP", "JOKE", "LIFE"};
    private String[] mStrArticleTagShow = {"所有文章", "表特", "八卦", "就可", "生活"};

    // Firebase Database
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    FirebaseHelper firebaseHelper = new FirebaseHelper();

    // post Users
    private ConstraintLayout mConstraintLayoutUserLogin;
    private EditText mEditTextNameLoginEmail;
    private EditText mEditTextNameLoginPw;
    private EditText mEditTextNameRegisterName;
    private EditText mEditTextNameRegisterEmail;
    private EditText mEditTextNameRegisterPw;
    private TextView mTextViewUserRegisterEmail;
    private Button mButtonUserLogin;
    private Button mButtonUserRegister;

    // search users
    private Button mButtonSearchUserEmail;
    private TextView mTextViewSearchUserEmail;
    private String mStrFriendId;
    private TextView mTextViewFriendStatus;
    private Button mButtonFriendStatus;


    // search articles
    private Button mButtonSearchArticles;
    private EditText mEditTextSearchArticles;
    private String mStrSearchArticleFilterTag = null;

    // post articles
    private Button mButtonPostArticles;
    private EditText mEditPostArticleTitle;
    private EditText mEditPostArticleContent;
    private String mStrPostArticleTag = null;


    private int intSearchArticleTagId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();


        //**
        // [Search Function] 2. Articles
        // Search by tags
        // [Spinner]
        Spinner spinnerSearchArticleTag = (Spinner)findViewById(R.id.spinner_search_article_tag);

        ArrayAdapter<String> searchArticleTagList = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, // android 提供的佈局方式
                mStrArticleTagShow);

        spinnerSearchArticleTag.setAdapter(searchArticleTagList);

        spinnerSearchArticleTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(MainActivity.this, "歡迎來到" + mStrArticleTagShow[position] + "版", Toast.LENGTH_SHORT).show();
                mStrSearchArticleFilterTag = mStrArticleTagId[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // [Search Button]
        mEditTextSearchArticles = findViewById(R.id.edittext_search_article_author_email);
        mButtonSearchArticles = findViewById(R.id.button_search_article_send);
        mButtonSearchArticles.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // check if user input email in editText
                String strAuthor = mEditTextSearchArticles.getText().toString();
                String strFilter = mStrSearchArticleFilterTag;


                // [Firebase]
                Query queryData;

                // without constraint author filter
                if (mStrSearchArticleFilterTag == null || mStrSearchArticleFilterTag.equals("")) {

                    if ("ALL".equals(strFilter)) {
                        queryData = mDatabaseReference.child("article");
                    } else {
                        queryData = mDatabaseReference.child("article").orderByChild("tag").equalTo(strFilter);
                    }

                } else {

                    if ("ALL".equals(strFilter)) {
                        queryData = mDatabaseReference.child("article").orderByChild("author").equalTo(strAuthor);
                    } else {
                        queryData = mDatabaseReference.child("article").orderByChild("author_tag").equalTo(strAuthor + "_" + strFilter);
                    }
                }

                queryData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        // show all users has this email
                        // update UI

//                String value = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Value is: " + value);
//                String value = dataSnapshot.getValue(String.class);
                        Log.d(TAG, MSG + "Search articles onDataChange: dataSnapshot = " + dataSnapshot.toString());
                    }


                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.d(TAG, MSG + "Search articles onCancelled");
                    }
                });


            }
        }); // search articles by tag



        //**
        //[Post Function] 1. user login
        mConstraintLayoutUserLogin = findViewById(R.id.constraint_user_login);
        mEditTextNameLoginEmail = findViewById(R.id.edittext_user_login_email);
        mEditTextNameLoginPw = findViewById(R.id.edittext_user_login_password);
        mEditTextNameRegisterName = findViewById(R.id.edittext_user_register_name);
        mEditTextNameRegisterEmail = findViewById(R.id.edittext_user_register_email);
        mEditTextNameRegisterPw = findViewById(R.id.edittext_user_register_password);
        mTextViewUserRegisterEmail = findViewById(R.id.textview_user_register_email);

        //[Login]
        mButtonUserLogin = findViewById(R.id.button_user_login_send);
        mButtonUserLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 1. 取得 user login info
                final String strEmail = mEditTextNameLoginEmail.getText().toString();
                final String strPw = mEditTextNameLoginPw.getText().toString();

                // Email and password can't be null
                if (strEmail == null || strPw == null || strEmail.equals("") || strPw.equals("")) {
                    Toast.makeText(MainActivity.this, "Email and password can't be null", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }


                // 2. 把 user 的帳號丟進資料庫檢查是否已有此 user
                //    1) 如果可搜尋出來一個唯一的 ID 則讓他登入
                //	  2) 如果找不到這個 ID 則顯示錯誤

                // [Firebase] query
                DatabaseReference userDataRef = mDatabase.getReference("user");

                Query queryData = userDataRef.orderByChild("email").equalTo(strEmail);
                queryData.addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Log.d(TAG, MSG + "onChildAdded");
                        Log.d(TAG, MSG + "Login by email onDataChange: dataSnapshot = " + dataSnapshot.toString());

                        Boolean hasLoginCheck = false;

                        // search the specific email (user input)
                        String strData = dataSnapshot.child("email").getValue().toString();
                        Log.d(TAG, MSG + "[Login] dataSnapshot.child(\"email\").toString() = " + strData);

                        // if no matched email
                        if (strData == null) {

                            Toast.makeText(MainActivity.this, "Email is not existed!", Toast.LENGTH_SHORT).show();
                            hasLoginCheck = false;

                        } else {

                            // check password
                            if (dataSnapshot.hasChild("password")) {

                                String userPassword = dataSnapshot.child("password").getValue().toString();
                                if (strPw.equals(userPassword)) {
                                    hasLoginCheck = true;
                                }

                            } else {
                                // super user (don't have password)
                                hasLoginCheck = true;
                            }
                        }

                        // if login
                        if (hasLoginCheck) {

                            mStrUserEmail = mEditTextNameLoginEmail.getText().toString();
                            mStrUserId = dataSnapshot.getKey();
                            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                            mConstraintLayoutUserLogin.setVisibility(View.GONE);

                        } else {

                            mEditTextNameLoginEmail.setText("");
                            mEditTextNameLoginPw.setText("");
                            Toast.makeText(MainActivity.this, "Email or password is invalid. Please login again!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, MSG + "onChildChanged");
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d(TAG, MSG + "onChildRemoved");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, MSG + "onChildMoved");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, MSG + "onCancelled");
                    }
                });
            } //onclick user Login
        }); //ButtonUserLogin.setOnClickListener

//                queryData.addValueEventListener(new ValueEventListener() {
//
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // This method is called once with the initial value and again
//                        // whenever data at this location is updated.
//
//                        Log.d(TAG, MSG + "[Login] Search users by email onDataChange: dataSnapshot = " + dataSnapshot.toString());
//
//
////                        String strData = dataSnapshot.toString();//.child("user").child("email").toString();//getValue().toString();//.getValue().toString();
////                        String strUserId =  dataSnapshot.getRef().child("user").push().getKey();
////                        String strData = dataSnapshot.child("user").child(strUserId).toString();
//
//                        String strData = dataSnapshot.child("email").toString();
//
//
//                        Log.d(TAG, MSG + "[Login] dataSnapshot.getValue() = " + strData);
//
//                        Boolean hasLoginCheck = false;
//
//                        if (strData == null) {
//
//                            Toast.makeText(MainActivity.this, "Email is not existed!", Toast.LENGTH_SHORT).show();
//                            hasLoginCheck = false;
//
//                        } else {
//
//                            try {
//                                JSONObject jsonObjectUser = new JSONObject(strData);
//
//                                if (jsonObjectUser.has("password")) {
//
//                                    String userPassword = jsonObjectUser.getString("password");
//                                    if (strPw.equals(userPassword)) {
//                                        hasLoginCheck = true;
//                                    }
//                                } else { //super user
//                                    hasLoginCheck = true;
//                                }
//
//                            } catch (JSONException e) {
//                                Log.d(TAG, MSG + "User login parse json exception: " + e.toString());
//                                e.printStackTrace();
//                            }
//                        }
//
//
//
//                        // if login
//                        if (hasLoginCheck) {
//
//                            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
//                            mConstraintLayoutUserLogin.setVisibility(View.INVISIBLE);
//
//                        } else {
//
//                            mEditTextNameLoginEmail.setText("");
//                            mEditTextNameLoginPw.setText("");
//                            Toast.makeText(MainActivity.this, "Email or password is invalid. Please login again!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        // Failed to read value
//                        Log.d(TAG, MSG + "Search users by email onCancelled");
//                    }
//                });
//                //
//            }
//        }); // User Login


        // Register a new account
        mButtonUserRegister = findViewById(R.id.button_user_register_send);
        mButtonUserRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 1. 取得使用者輸入
                final String strName = mEditTextNameRegisterName.getText().toString();
                final String strEmail = mEditTextNameRegisterEmail.getText().toString();
                final String strPw = mEditTextNameRegisterPw.getText().toString();

                // 2. 把 user 的帳號丟進資料庫檢查是否已有此 user
                //    1) 如果已經存在，則跳出已有此使用者 id
                //	  2) 如果這個帳號還沒有人使用過，則成功註冊並跳轉頁面
                // [Firebase] query
                Query queryData = mDatabaseReference.child("user").orderByChild("email").equalTo(strEmail);
                queryData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        // show all users has this email
                        // update UI

                        if (dataSnapshot.getValue() == null) {
                            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();

                            // register
                            firebaseHelper.writeNewUser(strName, strEmail, strPw);
                            mConstraintLayoutUserLogin.setVisibility(View.GONE);

                        } else {
//                            Toast.makeText(MainActivity.this, "Email is already existed", Toast.LENGTH_SHORT).show();
                            mTextViewUserRegisterEmail.setText("Email: (x) Email is already existed");
                            Log.d(TAG, MSG + "Search user email on registration = " + dataSnapshot.toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.d(TAG, MSG + "register onCancelled");
                    }
                });
//                DatabaseReference userDataRef = mDatabase.getReference("user");
//
//                Query queryData = userDataRef.orderByChild("email").equalTo(strEmail);
//                queryData.addChildEventListener(new ChildEventListener() {
//
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                        Log.d(TAG, MSG + "onChildAdded");
//                        Log.d(TAG, MSG + "registration : dataSnapshot = " + dataSnapshot.toString());
//
//                        // search the specific email (user input)
//                        String strData = dataSnapshot.child("email").getValue().toString();
//                        Log.d(TAG, MSG + "[Login] dataSnapshot.child(\"email\").toString() = " + strData);
//
//                        // if no matched email
//                        if (strData == null) {
//
//                            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
//
//                            // register
//                            firebaseHelper.writeNewUser(strName, strEmail, strPw);
//                            mConstraintLayoutUserLogin.setVisibility(View.GONE);
//
//
//                        } else {
//                            Toast.makeText(MainActivity.this, "Email is already existed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                        Log.d(TAG, MSG + "onChildChanged");
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//                        Log.d(TAG, MSG + "onChildRemoved");
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                        Log.d(TAG, MSG + "onChildMoved");
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.d(TAG, MSG + "onCancelled");
//                    }
//                });
            }
        }); // user Register


        //**
        //[Post Function] 2. post articles
        // [Spinner]
        Spinner spinnerPostArticleTag = (Spinner) findViewById(R.id.spinner_post_article_tag);

        ArrayAdapter<String> postArticleTagList = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, // android 提供的佈局方式
                mStrArticleTagShow);

        spinnerPostArticleTag.setAdapter(postArticleTagList);

        spinnerPostArticleTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStrPostArticleTag = mStrArticleTagId[position];
                Log.d(TAG, MSG + "spinnerPostArticleTag.setOnItemSelectedListener: TAG = " + mStrPostArticleTag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // [Post Button]
        mEditPostArticleTitle = findViewById(R.id.edittext_post_article_title);
        mEditPostArticleContent = findViewById(R.id.edittext_post_article_content);
        mButtonPostArticles = findViewById(R.id.button_post_article_send);

        mButtonPostArticles.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String strTitle = mEditPostArticleTitle.getText().toString();
                String strContent = mEditPostArticleContent.getText().toString();
                String strTag = (("ALL".equals(mStrPostArticleTag)) ? "" : mStrPostArticleTag);
                String strAuthor = mStrUserId;

                SimpleDateFormat formatter = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss");
                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                String strDate = formatter.format(curDate);

                // post
                firebaseHelper.writeNewArticle(mStrUserId, strTitle, strContent, strTag, strDate);
            }
        }); // post articles



        //**
        // [Search Function] 1. user
        // Search by email
        mTextViewSearchUserEmail = findViewById(R.id.edittext_search_user_email);
        mButtonSearchUserEmail = findViewById(R.id.button_search_user_email_send);
        mTextViewFriendStatus = findViewById(R.id.textview_search_friend_status);

        mButtonSearchUserEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // check if user input email in editText
                String strEmailInput = mTextViewSearchUserEmail.getText().toString();

                // return if user input null
                if (strEmailInput.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter the email", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // [Firebase] query
                DatabaseReference userDataRef = mDatabase.getReference("user");

                Query queryData = userDataRef.orderByChild("email").equalTo(strEmailInput);
                queryData.addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Log.d(TAG, MSG + "onChildAdded");
                        Log.d(TAG, MSG + "Search user by email onDataChange: dataSnapshot = " + dataSnapshot.toString());
                        Log.d(TAG, MSG + "Search user by email onDataChange: getKey = " + dataSnapshot.getKey());


                        if (dataSnapshot.getKey() == null) {
                            return;
                        }

                        mStrFriendId = dataSnapshot.getKey().toString();

                        DatabaseReference friendDataRef = mDatabase.getReference("user/" + mStrUserId + "/friends");

                        Query queryFriends = friendDataRef.orderByChild(mStrFriendId).limitToFirst(1);
                        queryFriends.addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                Log.d(TAG, MSG + "onChildAdded: QUERY in friend status: " + dataSnapshot.toString());
                                if (dataSnapshot.getValue() == null) {
                                    Log.d(TAG, MSG + "query friends: getValue() = null");
                                }

                                String strFriendStatus = dataSnapshot.getValue().toString();
                                if (strFriendStatus.equals("friends")) {
                                    mButtonFriendStatus.setText("Friend :)");

                                } else if (strFriendStatus.equals("invited")) {
                                    //我送邀請對方還沒回應
                                    mButtonFriendStatus.setText("Cancel!");

                                } else if (strFriendStatus.equals("to be confirmed")) {
                                    //對方送邀請給我還沒回應
                                    mButtonFriendStatus.setText("Confirm!");

                                } else {
                                    Log.d(TAG, MSG + "query friends: strFriendStatus = " + strFriendStatus);
                                }


                                mTextViewFriendStatus.setText(dataSnapshot.getValue().toString());



                                // 更換按鈕
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, MSG + "onChildChanged");
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d(TAG, MSG + "onChildRemoved");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, MSG + "onChildMoved");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, MSG + "onCancelled");
                    }
                });
                // [Firebase]
//                Query queryData = mDatabaseReference.child("user").orderByChild("email").equalTo(strEmailInput);
//
//                queryData.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // This method is called once with the initial value and again
//                        // whenever data at this location is updated.
//
//                        // show all users has this email
//                        // update UI
//
////                String value = dataSnapshot.getValue(String.class);
////                Log.d(TAG, "Value is: " + value);
////                String value = dataSnapshot.getValue(String.class);
//                        Log.d(TAG, MSG + "Search users by email onDataChange: dataSnapshot = " + dataSnapshot.toString());
//                    }
//
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        // Failed to read value
//                        Log.d(TAG, MSG + "Search users by email onCancelled");
//                    }
//                });


            }
        }); // search users by email


        //**
        //[Update Function] 3. add friends
        mTextViewFriendStatus = findViewById(R.id.textview_search_friend_status);
        mButtonFriendStatus = findViewById(R.id.button_search_friend_status);

        mButtonFriendStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mButtonFriendStatus.getText() == "friends") {
                    // 按了沒事
                    ;
                } else if (mButtonFriendStatus.getText() == "invited") {
                    // 按了就會 cancel (remove)

                    //mStrFriendId
                } else if (mButtonFriendStatus.getText() == "to be confirmed") {
                    // 按了就會改狀態 setvalue

                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.setValue("friends");

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.setValue("friends");

                } else { //invite
                    // 按了就會邀請 put
                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.setValue("to be confirmed");

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.setValue("invited");

                }

            } //onclick user Login
        }); //ButtonUserLogin.setOnClickListener

    }
}
