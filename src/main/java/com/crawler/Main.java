package com.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection;
        connection = DriverManager.getConnection("jdbc:h2:file:/Users/shenwl/Projects/java-crawler-project/news");

        while (true) {
            // waiting for process
            List<String> linkPool = loadUrlsFromDb(connection, "select link from LINKS_TO_BE_PROCESSED");
            // already processed

            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size() - 1);

            if (linkHasProcessed(connection, link) || !isSinaNewsLink(link)) {
                continue;
            }

            // 从待处理link池取出，放入已处理池
            processLink(connection, link);

            Document doc = requestAndParseHtml(link);

            parseLinksFromPageAndStoreIntoDatabase(connection, doc);

            parseNewsFromPageAndStoreInfoDatabase(connection, doc, link);
        }

    }

    private static void parseNewsFromPageAndStoreInfoDatabase(Connection con, Document doc, String link) throws SQLException {
//        ArrayList<Element> articles = doc.select("article");
//
//        for (Element article : articles) {
//            News news = getNewsFromArticleEl(article, link);
//        }
    }

    private static News getNewsFromArticleEl(Element article, String link) {
        String title = article.select(".art_tit_h1").get(0).text();
        String content = article.select(".art_tit_h1").get(0).text();

        return News.createNews(link, title, content);
    }

    private static void parseLinksFromPageAndStoreIntoDatabase(Connection con, Document doc) throws SQLException {
        List<String> links = getLinksFromDoc(doc);
        
        for(String link : links) {
            insertLinkIntoDataBase(con,"insert into LINKS_TO_BE_PROCESSED (link) values (?)", link);
        }
    }

    private static void insertLinkIntoDataBase(Connection con, String sql, String link) throws SQLException {
        PreparedStatement state = con.prepareStatement(sql);
        state.setString(1, link);
        state.executeUpdate();
    }

    private static boolean linkHasProcessed(Connection con, String link) throws SQLException {
        boolean processed = false;

        PreparedStatement state = con.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?");
        state.setString(1, link);
        ResultSet resultSet = state.executeQuery();

        while (resultSet.next()) {
            processed = true;
        }
        return processed;
    }

    private static boolean isSinaNewsLink(String link) {
        if (link.contains("passport.sina.cn")) {
            return false;
        }
        return link.contains("news.sina.cn") || "https://sina.cn".equals(link);
    }

    private static ArrayList<String> getLinksFromDoc(Document doc) {
        ArrayList<Element> linkTags = doc.select("a");
        ArrayList<String> links = new ArrayList<>();

        linkTags.stream().map(linkTag -> linkTag.attr("href"))
                .filter(Main::isSinaNewsLink)
                .forEach(links::add);

        return links;
    }

    private static void setCrawlerHeader(HttpRequestBase request) {
        request.setHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36"
        );
    }

    private static ArrayList<String> loadUrlsFromDb(Connection con, String sql) throws SQLException {
        ArrayList<String> urls = new ArrayList<>();

        PreparedStatement preparedStatement = con.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            urls.add(resultSet.getString(1));
        }
        return urls;
    }

    private static void processLink(Connection con, String link) throws SQLException {
        PreparedStatement statement1 = con.prepareStatement("DELETE FROM LINKS_TO_BE_PROCESSED where link = ?");
        statement1.setString(1, link);
        statement1.executeUpdate();

        insertLinkIntoDataBase(con,"insert into LINKS_ALREADY_PROCESSED (link) values (?)", link);
    }

    private static Document requestAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        setCrawlerHeader(httpGet);
        try (CloseableHttpResponse res = httpClient.execute(httpGet)) {
            System.out.println(res.getStatusLine());
            System.out.println(link);

            HttpEntity entity = res.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        }
    }

    private static void printTitle(ArrayList<Element> articleTags) {
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }
}
