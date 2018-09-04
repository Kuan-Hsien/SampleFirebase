package com.kuanhsien.samplefirebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Article {
    private String mId;                     //Article id.
    private String mUserId;                 //Author of the article.
    private String mTitle;                  //Article title.
    private String mContent;                //Article content.
    private String mTag;                    //Article create time.
    private String mCreatedTime;            //Article create time.
    private String mFilter;

    public Article(String id, String userId, String title, String content, String tag, String createdTime) {
        mId = id;
        mUserId = userId;
        mTitle = title;
        mContent = content;
        mTag = tag;
        mCreatedTime = createdTime;
        mFilter = userId + "_" + tag;
    }

    public Article(Article article) {
        mId = article.getId();
        mUserId = article.getUserId();
        mTitle = article.getTitle();
        mContent = article.getContent();
        mTag = article.getTag();
        mCreatedTime = article.getCreatedTime();
        mFilter = article.getFilter();
    }

    public Article() {
        mId = "";
        mUserId = "";
        mTitle = "";
        mContent = "";
        mTag = "";
        mCreatedTime = "";
        mFilter = "";
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("article_id", mId);
        result.put("author", mUserId);
        result.put("article_title", mTitle);
        result.put("article_content", mContent);
        result.put("article_tag", mTag);
        result.put("created_time", mCreatedTime);
        result.put("author_tag", mFilter);

        return result;
    }

    //get values
    public String getId() {
        return mId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public String getTag() {
        return mTag;
    }

    public String getCreatedTime() {
        return mCreatedTime;
    }

    public String getFilter() {
        return mFilter;
    }

    //set attributes
    public void setId(String id) {
        this.mId = id;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setCreatedTime(String createdTime) {
        this.mCreatedTime = createdTime;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }
}

