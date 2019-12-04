package com.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    SqlSession session;

    public MyBatisCrawlerDao() {
        try {
            String resource  = "resources/db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            this.session = new SqlSessionFactoryBuilder().build(inputStream).openSession();
        } catch (IOException e) {
            System.err.println("MyBatis连接失败：" + e.getMessage());
        }
    }

    @Override
    public String getNextLinkThenDelete() {
        String link = session.selectOne("com.crawler.MyMapper.selectNextAvailableLink");
        if(link != null) {
            session.delete("com.crawler.MyMapper.deleteLink", link);
        }
        return link;
    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) {
        Integer count = session.selectOne("com.crawler.MyMapper.countLinkInAlreadyProcessed", link);
        return count != 0;
    }

    @Override
    public void insertLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("isAlreadyProcessed", false);
        param.put("link", link);
        session.insert("com.crawler.MyMapper.insertLink", param);
    }

    @Override
    public void processLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("isAlreadyProcessed", true);
        param.put("link", link);
        session.insert("com.crawler.MyMapper.insertLink", param);
    }

    @Override
    public void insertNews(News news) {
        session.insert("com.crawler.MyMapper.insertNews", news);
    }
}
