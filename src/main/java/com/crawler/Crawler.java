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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String link = null;

            while ((link = dao.getNextLinkThenDelete()) != null) {
                if (dao.isLinkAlreadyProcessed(link) || !isSinaNewsLink(link)) {
                    continue;
                }

                Document doc = requestAndParseHtml(link);

                parseLinksFromPageAndStoreIntoDatabase(doc);

                parseNewsFromPageAndStoreInfoDatabase(doc, link);

                // 从待处理link池取出，放入已处理池
                dao.processLink(link);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseNewsFromPageAndStoreInfoDatabase(Document doc, String link) {
        ArrayList<Element> articles = doc.select("article");

        if (!articles.isEmpty()) {
            for (Element article : articles) {
                News news = getNewsFromArticleEl(article, link);
                dao.insertNews(news);
            }
        }
    }

    private News getNewsFromArticleEl(Element article, String link) {
        String title = article.select(".art_tit_h1").get(0).text();
        ArrayList<Element> paragraphs = article.select("p");

        String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));

        return News.createNews(link, title, content);
    }

    private void parseLinksFromPageAndStoreIntoDatabase(Document doc) {
        List<String> links = getLinksFromDoc(doc);

        for (String link : links) {
            dao.insertLink(link);
        }
    }


    private static boolean isSinaNewsLink(String link) {
        if (link.contains("passport.sina.cn")) {
            return false;
        }
        return link.contains("news.sina.cn") || "https://sina.cn".equals(link);
    }

    private String parseLink(String link) {
        // 去除"\"
        return link.replaceAll("\\\\", "");
    }

    private ArrayList<String> getLinksFromDoc(Document doc) {
        ArrayList<Element> linkTags = doc.select("a");
        ArrayList<String> links = new ArrayList<>();

        linkTags.stream().map(linkTag -> linkTag.attr("href"))
                .filter(Crawler::isSinaNewsLink)
                .map(link -> parseLink(link))
                .forEach(links::add);

        return links;
    }

    private void setCrawlerHeader(HttpRequestBase request) {
        request.setHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36"
        );
    }

    private Document requestAndParseHtml(String link) throws IOException {
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
}
