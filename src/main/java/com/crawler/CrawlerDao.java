package com.crawler;

public interface CrawlerDao {
    String getNextLinkThenDelete();

    boolean isLinkAlreadyProcessed(String link);

    void insertLink(String link);

    void processLink(String link);

    void insertNews(News news);
}
