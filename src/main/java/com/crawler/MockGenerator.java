package com.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockGenerator {
    public static final int TARGET_NEWS_COUNT = 100_0000;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;

        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            copyNews(sqlSessionFactory);
        } catch (IOException e) {
            System.err.println("MyBatis连接失败：" + e.getMessage());
        }
    }

    public static void copyNews(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNewsList = session.selectList("com.crawler.MockMapper.selectNews");
            int currentNewsSize = currentNewsList.size();

            int count = TARGET_NEWS_COUNT - currentNewsSize;
            Random random = new Random();

            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNewsSize);
                    News newsToBeInsert = currentNewsList.get(index);
                    Instant currentTime = newsToBeInsert.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreatedAt(currentTime);
                    newsToBeInsert.setModifiedAt(currentTime);

                    session.insert("com.crawler.MockMapper.insertNews", newsToBeInsert);
                    System.out.println("count left: " + count);
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
