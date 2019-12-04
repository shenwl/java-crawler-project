package com.crawler;

public interface CrawlerDao {
    String getNextLinkThenDelete();

    boolean linkHasProcessed(String link);

    void insertLink(String link);

    void processLink(String link);

    void insertNews(News news);
}
