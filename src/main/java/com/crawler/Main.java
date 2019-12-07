package com.crawler;

public class Main {
    static Integer threadCount = 6;

    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();

        for (int i = 0; i < threadCount; i++) {
            new Crawler(dao).start();
        }
    }
}
