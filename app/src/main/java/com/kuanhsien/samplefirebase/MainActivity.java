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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;

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
    private ConstraintLayout mConstraintLayoutSearchUser;
    private ConstraintLayout mConstraintLayoutSearchArticle;
    private ConstraintLayout mConstraintLayoutPostArticle;
    private ConstraintLayout mConstraintLayoutUserLogin;
    private EditText mEditTextUserLoginEmail;
    private EditText mEditTextUserLoginPw;
    private EditText mEditTextUserRegisterName;
    private EditText mEditTextUserRegisterEmail;
    private EditText mEditTextUserRegisterPw;
    private TextView mTextViewUserRegisterEmail;
    private Button mButtonUserLogin;
    private Button mButtonUserRegister;

    // search users
    private Button mButtonSearchUserEmail;
    private EditText mEditTextSearchUserEmail;
    private String mStrFriendId;
    private String mStrFriendEmail;
    private String mStrFriendStatus;
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


    Boolean isFirstQueryArticle;
    Boolean isFirstUserLogin;
    Boolean isFirstUserRegister;
    Boolean isFirstQueryFriendId;
    Boolean isFirstQueryFriendStatus;

    private Query mQueryArticle;
    private Query mQueryUserLoginData;
    private Query mQueryUserRegisterData;
    private Query mQueryFriendId;
    private DatabaseReference mUserDataRef;
    private Query mQueryFriendsStatus;
    private DatabaseReference mFriendDataRef;



    private String strEmail;
    private String strPw;
    private String strName;

    @Override
    protected void onResume() {
        super.onResume();
        isFirstQueryArticle = true;
        isFirstUserLogin = true;
        isFirstUserRegister = true;
        isFirstQueryFriendId = true;
        isFirstQueryFriendStatus = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFirstQueryArticle == false) { mQueryArticle.removeEventListener(mEventListenerQueryArticle); };
        if (isFirstUserLogin == false) { mQueryUserLoginData.removeEventListener(mChildEventListenerSearchUserEmail); };
//        if (isFirstUserRegister == false) { mQueryUserRegisterData.removeEventListener(mEventListenerQuery); };
        if (isFirstQueryFriendId == false) { mQueryFriendId.removeEventListener(mChildEventListenerSearchFriendId); };
        if (isFirstQueryFriendStatus == false) { mQueryFriendsStatus.removeEventListener(mChildEventListenerSearchFriendStatus); };

        isFirstQueryArticle = true;
        isFirstUserLogin = true;
        isFirstUserRegister = true;
        isFirstQueryFriendId = true;
        isFirstQueryFriendStatus = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isFirstQueryArticle = true;
        isFirstUserLogin = true;
        isFirstUserRegister = true;
        isFirstQueryFriendId = true;
        isFirstQueryFriendStatus = true;

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

//                Toast.makeText(MainActivity.this, "歡迎來到" + mStrArticleTagShow[position] + "版", Toast.LENGTH_SHORT).show();
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
                if (isFirstQueryArticle) {
                    isFirstQueryArticle = false;
                } else {
                    mQueryArticle.removeEventListener(mEventListenerQueryArticle);
                }
                // without constraint author filter
                if (mStrSearchArticleFilterTag == null || mStrSearchArticleFilterTag.equals("")) {

                    if ("ALL".equals(strFilter)) {
                        mQueryArticle = mDatabaseReference.child("article");
                    } else {
                        mQueryArticle = mDatabaseReference.child("article").orderByChild("tag").equalTo(strFilter);
                    }

                } else {

                    if ("ALL".equals(strFilter)) {
                        mQueryArticle = mDatabaseReference.child("article").orderByChild("author").equalTo(strAuthor);
                    } else {
                        mQueryArticle = mDatabaseReference.child("article").orderByChild("author_tag").equalTo(strAuthor + "_" + strFilter);
                    }
                }

                mQueryArticle.addValueEventListener(mEventListenerQueryArticle);


            }
        }); // search articles by tag

        //**
        //[Post Function] 1. user login
        mConstraintLayoutUserLogin = findViewById(R.id.constraint_user_login);
        mConstraintLayoutSearchUser = findViewById(R.id.constraint_search_user_email);
        mConstraintLayoutSearchArticle = findViewById(R.id.constraint_search_article);
        mConstraintLayoutPostArticle = findViewById(R.id.constraint_post_article);

        mEditTextUserLoginEmail = findViewById(R.id.edittext_user_login_email);
        mEditTextUserLoginPw = findViewById(R.id.edittext_user_login_password);
        mEditTextUserRegisterName = findViewById(R.id.edittext_user_register_name);
        mEditTextUserRegisterEmail = findViewById(R.id.edittext_user_register_email);
        mEditTextUserRegisterPw = findViewById(R.id.edittext_user_register_password);
        mTextViewUserRegisterEmail = findViewById(R.id.textview_user_register_email);

        //[Login]
        mButtonUserLogin = findViewById(R.id.button_user_login_send);
        mButtonUserLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Email and password can't be null
                if (mEditTextUserLoginEmail.getText() == null || mEditTextUserLoginPw.getText() == null || mEditTextUserLoginEmail.getText().toString().equals("") || mEditTextUserLoginPw.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Email and password can't be null", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. 取得 user login info
                strEmail = mEditTextUserLoginEmail.getText().toString();
                strPw = EncodeMd5.md5Password(mEditTextUserLoginPw.getText().toString());

                Log.d(TAG, MSG + "Login password: " + strPw);

                // 2. 把 user 的帳號丟進資料庫檢查是否已有此 user
                //    1) 如果可搜尋出來一個唯一的 ID 則讓他登入
                //	  2) 如果找不到這個 ID 則顯示錯誤

                // [Firebase] query
                DatabaseReference userDataRef = mDatabase.getReference("user");

                // remove current listener
                if (isFirstUserLogin) {
                    isFirstUserLogin = false;
                } else {
                    mQueryUserLoginData.removeEventListener(mChildEventListenerSearchUserEmail);
                }
                mQueryUserLoginData = userDataRef.orderByChild("email").equalTo(strEmail);
                mQueryUserLoginData.addChildEventListener(mChildEventListenerSearchUserEmail);

            } //onclick user Login
        }); //ButtonUserLogin.setOnClickListener

        // Register a new account
        mButtonUserRegister = findViewById(R.id.button_user_register_send);
        mButtonUserRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 1. 取得使用者輸入

                if (mEditTextUserRegisterName.getText() == null
                        || mEditTextUserRegisterName.getText().toString().equals("")
                        || mEditTextUserRegisterEmail.getText() == null
                        || mEditTextUserRegisterEmail.getText().toString().equals("")
                        || mEditTextUserRegisterPw.getText() == null
                        || mEditTextUserRegisterPw.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "所有欄位均為必填", Toast.LENGTH_SHORT).show();
                    return;
                }

                strName = mEditTextUserRegisterName.getText().toString();
                strEmail = mEditTextUserRegisterEmail.getText().toString();
                strPw = EncodeMd5.md5Password(mEditTextUserRegisterPw.getText().toString());

                Log.d(TAG, MSG + "Register password: " + strPw);

                // 2. 把 user 的帳號丟進資料庫檢查是否已有此 user
                //    1) 如果已經存在，則跳出已有此使用者 id
                //	  2) 如果這個帳號還沒有人使用過，則成功註冊並跳轉頁面
                // [Firebase] query
//                if (isFirstUserRegister) {
//                    isFirstUserRegister = false;
//                } else {
//                    mQueryUserRegisterData.removeEventListener();
//                }
                mQueryUserRegisterData = mDatabaseReference.child("user").orderByChild("email").equalTo(strEmail);
                mQueryUserRegisterData.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            mConstraintLayoutSearchUser.setVisibility(View.VISIBLE);
                            mConstraintLayoutPostArticle.setVisibility(View.VISIBLE);
                            mConstraintLayoutSearchArticle.setVisibility(View.VISIBLE);

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
        mEditTextSearchUserEmail = findViewById(R.id.edittext_search_user_email);
        mButtonSearchUserEmail = findViewById(R.id.button_search_user_email_send);
        mTextViewFriendStatus = findViewById(R.id.textview_search_friend_status);

        mButtonSearchUserEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //initialization
                mStrFriendId = "";
                mStrFriendEmail = "";
                mStrFriendStatus = "";
                mTextViewFriendStatus.setText("Email is not exised");
                mButtonFriendStatus.setText("Invite");
                mButtonFriendStatus.setVisibility(View.INVISIBLE);

                // check if user input email in editText
                String strEmailInput = mEditTextSearchUserEmail.getText().toString();

                // return if user input null
                if (strEmailInput.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter the email", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                mStrFriendEmail = strEmailInput;


                // [Firebase] query
                mUserDataRef = mDatabase.getReference("user");

                if (isFirstQueryFriendId) {
                    isFirstUserLogin = false;
                } else {
                    mQueryFriendId.removeEventListener(mChildEventListenerSearchFriendId);
                }
                mQueryFriendId = mUserDataRef.orderByChild("email").equalTo(strEmailInput);
                mQueryFriendId.addChildEventListener(mChildEventListenerSearchFriendId);

            }
        }); // search users by email


        //**
        //[Update Function] 3. add friends
        mTextViewFriendStatus = findViewById(R.id.textview_search_friend_status);
        mButtonFriendStatus = findViewById(R.id.button_search_friend_status);

        mButtonFriendStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mStrFriendStatus.equals("friends")) {
                    // 按了就取消好友
                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.removeValue();

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.removeValue();

                    mStrFriendStatus = "";
                    mTextViewFriendStatus.setText("");
                    mButtonFriendStatus.setText("Invite");

                } else if (mStrFriendStatus.equals("invited")) {
                    // 按了就會 cancel (remove)

                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.removeValue();

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.removeValue();

                    mStrFriendStatus = "";
                    mTextViewFriendStatus.setText("");
                    mButtonFriendStatus.setText("Invite");

                    //mStrFriendId
                } else if (mStrFriendStatus.equals("to be confirmed")) {
                    // 按了就會改狀態 setvalue

                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.setValue("friends");

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.setValue("friends");

                    mStrFriendStatus = "friends";
                    mTextViewFriendStatus.setText("Congratulation!");
                    mButtonFriendStatus.setText("Un-friend");

                } else { //invite
                    // 按了就會邀請 put
                    DatabaseReference myRef = mDatabase.getReference("user/" + mStrUserId + "/friends/" + mStrFriendId);
                    myRef.setValue("invited");

                    DatabaseReference friendsRef = mDatabase.getReference("user/" + mStrFriendId + "/friends/" + mStrUserId);
                    friendsRef.setValue("to be confirmed");

                    mStrFriendStatus = "invited";
                    mTextViewFriendStatus.setText("Status: invitation sent!");
                    mButtonFriendStatus.setText("Cancel");
                }

            } //onclick user Login
        }); //ButtonUserLogin.setOnClickListener

    } //end of onCreate


    private ValueEventListener mEventListenerQueryArticle = new ValueEventListener() {
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
    };



    private ChildEventListener mChildEventListenerSearchFriendStatus = new ChildEventListener() {

        // 第一次會進來 + 後續每次 invited 都會進來
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.d(TAG, MSG + "onChildAdded: friends status: " + dataSnapshot.toString());
            if (dataSnapshot.getValue() == null) {
                Log.d(TAG, MSG + "query friends: getValue() = null");
            }

            mStrFriendStatus = dataSnapshot.getValue().toString();
            if (mStrFriendStatus.equals("friends")) {
                mButtonFriendStatus.setText("Un-friend");
                mTextViewFriendStatus.setText("Status: " + mStrFriendStatus);

            } else if (mStrFriendStatus.equals("invited")) {
                //我送邀請對方還沒回應
                mButtonFriendStatus.setText("Cancel");
                mTextViewFriendStatus.setText("Status: " + mStrFriendStatus);

            } else if (mStrFriendStatus.equals("to be confirmed")) {
                //對方送邀請給我還沒回應
                mButtonFriendStatus.setText("Confirm");
                mTextViewFriendStatus.setText("Status: " + mStrFriendStatus);

            } else {
                mButtonFriendStatus.setText("Invite");
                mTextViewFriendStatus.setText("");
            }

            Log.d(TAG, MSG + "query friends: mStrFriendStatus = " + mStrFriendStatus);
        }

        // 修改 db 內容時會進來
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            Log.d(TAG, MSG + "onChildChanged: friends status: " + dataSnapshot.toString());
            if (dataSnapshot.getValue() == null) {
                Log.d(TAG, MSG + "query friends: getValue() = null");
            }

            //如果原本狀態是邀請對方或受邀請還沒回應，進來代表雙方變成朋友
            mStrFriendStatus = "friends";
            mButtonFriendStatus.setText("Un-friend");
            mTextViewFriendStatus.setText("Status: " + mStrFriendStatus);

            Log.d(TAG, MSG + "query friends: mStrFriendStatus = " + mStrFriendStatus);
        }

        // 取消或刪除好友時會進來
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            Log.d(TAG, MSG + "onChildRemoved: friends status: " + dataSnapshot.toString());
            if (dataSnapshot.getValue() == null) {
                Log.d(TAG, MSG + "query friends: getValue() = null");
            }

            //進來代表被取消好友了
            mStrFriendStatus = "";
            mButtonFriendStatus.setText("Invite");
            mTextViewFriendStatus.setText("");

            Log.d(TAG, MSG + "query friends: mStrFriendStatus = " + mStrFriendStatus);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            Log.d(TAG, MSG + "onChildMoved: friends status: " + dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, MSG + "onCancelled");
        }
    };

    private ChildEventListener mChildEventListenerSearchFriendId = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.d(TAG, MSG + "onChildAdded");
            Log.d(TAG, MSG + "Search user by email onDataChange: dataSnapshot = " + dataSnapshot.toString());
            Log.d(TAG, MSG + "Search user by email onDataChange: getKey = " + dataSnapshot.getKey());


            if (dataSnapshot.getKey() == null) {
                return;
            }

            mTextViewFriendStatus.setText("");
            mButtonFriendStatus.setVisibility(View.VISIBLE);
            mStrFriendId = dataSnapshot.getKey().toString();

            mFriendDataRef = mDatabase.getReference("user/" + mStrUserId + "/friends");
//                        DatabaseReference friendDataRef = mDatabase.getReference("user/" + mStrUserId);

            if (isFirstQueryFriendStatus) {
                isFirstQueryFriendStatus = false;
            } else {
                mQueryFriendsStatus.removeEventListener(mChildEventListenerSearchFriendStatus);
            }
            mQueryFriendsStatus = mFriendDataRef.orderByKey().equalTo(mStrFriendId);
//                        Query queryFriends = friendDataRef.orderByChild(mStrFriendId).limitToFirst(1);
//                        Query queryFriends = friendDataRef.orderByChild("friends").equalTo(mStrFriendId);
            mQueryFriendsStatus.addChildEventListener(mChildEventListenerSearchFriendStatus);
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
    };

    // search user email
    private ChildEventListener mChildEventListenerSearchUserEmail = new ChildEventListener() {

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

                mStrUserEmail = mEditTextUserLoginEmail.getText().toString();
                mStrUserId = dataSnapshot.getKey();
                Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                mConstraintLayoutUserLogin.setVisibility(View.GONE);
                mConstraintLayoutSearchUser.setVisibility(View.VISIBLE);
                mConstraintLayoutPostArticle.setVisibility(View.VISIBLE);
                mConstraintLayoutSearchArticle.setVisibility(View.VISIBLE);

            } else {

                mEditTextUserLoginEmail.setText("");
                mEditTextUserLoginPw.setText("");
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
    };
}
