package com.crawler;

public interface CrawlerDao {
    String getNextLink();

    void insertLink(String sql, String link);

    boolean linkHasProcessed(String link);

    void processLink(String link);

    void insertNews(News news);
}
