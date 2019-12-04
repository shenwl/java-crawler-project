package com.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcCrawlerDao implements CrawlerDao {
    Connection connection;

    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/shenwl/Projects/java-crawler-project/news");
        } catch (SQLException e) {
            System.err.println("数据库连接失败：" + e.getMessage());
        }
    }

    private ArrayList<String> loadUrls(String sql) {
        ArrayList<String> urls = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                urls.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("loadUrls failed" + e.getMessage());
        }
        return urls;
    }

    private void deleteLink(String link) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where link = ?");
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("deleteLink failed" + e.getMessage());
        }
    }

    @Override
    public String getNextLinkThenDelete() {
        List<String> linkPool = loadUrls("select link from LINKS_TO_BE_PROCESSED limit 1");
        if (linkPool.isEmpty()) {
            return null;
        }
        String link = linkPool.get(0);
        deleteLink(link);
        return link;
    }

    @Override
    public void insertLink(String link) {
        try {
            PreparedStatement state = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (link) values (?)");
            state.setString(1, link);
            state.executeUpdate();
        } catch (SQLException e) {
            System.err.println("insertLink failed" + e.getMessage());
        }
    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) {
        boolean processed = false;

        try {
            PreparedStatement state = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?");
            state.setString(1, link);
            ResultSet resultSet = state.executeQuery();

            while (resultSet.next()) {
                processed = true;
            }
        } catch (SQLException e) {
            System.err.println("linkHasProcessed failed" + e.getMessage());
        }

        return processed;
    }

    @Override
    public void processLink(String link) {
        try {
            PreparedStatement state = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED (link) values (?)");
            state.setString(1, link);
            state.executeUpdate();
        } catch (SQLException e) {
            System.err.println("processLink failed" + e.getMessage());
        }
    }

    @Override
    public void insertNews(News news) {
        try {
            PreparedStatement state = connection.prepareStatement("insert into NEWS (URL, TITLE, CONTENT, CREATED_AT, MODIFIED_AT) values (?, ?, ?, ?, ?)");
            state.setString(1, news.getUrl());
            state.setString(2, news.getTitle());
            state.setString(3, news.getContent());
            state.setTimestamp(4, news.getCreatedAt());
            state.setTimestamp(5, news.getModifiedAt());
            state.executeUpdate();
        } catch (SQLException e) {
            System.err.println("insertNews failed" + e.getMessage());
        }
    }
}