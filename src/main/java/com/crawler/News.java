package com.crawler;

import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;

public class News {
    String url;
    String title;
    String content;
    Timestamp createdAt;
    Timestamp modifiedAt;

    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.createdAt = new Timestamp(currentTimeMillis());
        this.modifiedAt = new Timestamp(currentTimeMillis());
    }

    public static News createNews(String url, String title, String content) {
        return new News(url, title, content);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
