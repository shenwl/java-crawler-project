package com.crawler;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchDataMock {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory = null;

        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            System.err.println("MyBatis连接失败：" + e.getMessage());
        }

        List<News> newsList = getNewsFromMySQL(sqlSessionFactory);

        for (int i = 0; i < 8; i++) {
            new Thread(() -> writeData(newsList)).start();
        }
    }

    private static void writeData(List<News> newsList) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            BulkRequest bulkRequest = new BulkRequest();
            for (News news : newsList) {
                IndexRequest req = new IndexRequest("news");

                Map<String, Object> data = new HashMap<>();
                data.put("content", news.getContent());
                data.put("url", news.getUrl());
                data.put("title", news.getTitle());

                req.source(data, XContentType.JSON);
                bulkRequest.add(req);
            }
            BulkResponse res = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(res.status());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<News> getNewsFromMySQL(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            return session.selectList("com.crawler.MockMapper.selectNews");
        }
    }
}
