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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static boolean isSinaNewsLink(String link) {
        if (link.contains("passport.sina.cn")) {
            return false;
        }
        return link.contains("news.sina.cn") || "https://sina.cn".equals(link);
    }

    private static ArrayList<Element> getArticleTags(Document doc) {
        return doc.select("article");
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

    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://sina.cn");

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size() - 1);

            if (processedLinks.contains(link) || !isSinaNewsLink(link)) {
                continue;
            }

            Document doc = requestAndParseHtml(link);

            linkPool.addAll(getLinksFromDoc(doc));

            ArrayList<Element> articleTags = getArticleTags(doc);

            printTitle(articleTags);

            processedLinks.add(link);
        }
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
