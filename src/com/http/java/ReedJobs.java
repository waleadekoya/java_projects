package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ReedJobs extends QueryBuilder implements Runnable {

    static DataModel dm = new DataModel();
    static final int results_per_page = 25;
    private static final String ReedMainUrl = "https://www.reed.co.uk";

    public static ArrayList<String> DynamicReedUrls(Document doc) {
        ArrayList<String> urlsByPage = new ArrayList<>();
        int resultsCount = resultsCount(doc);
        System.out.println("Reed results_count is : " + resultsCount);
        if (resultsCount < results_per_page) {
            // if the number of results found is not more than a single page
            System.out.println(reedBaseUrl());
            return new ArrayList<>(Collections.singleton(reedBaseUrl()));
        } else for (int i = 1; i < getPagination(doc) + 1; i++) {
                urlsByPage.add(PaginationUrl(String.valueOf(i)));
        }
        System.out.println(urlsByPage);
        return urlsByPage;
    }

    public static String reedBaseUrl() {
        String urlExtension;
        if (isContract()) {
            urlExtension = "/jobs/%s-jobs?salaryfrom=%s&contract=True&datecreatedoffset=%s";
        } else urlExtension = "/jobs/%s-jobs?&salaryfrom=%s&datecreatedoffset=%s";
        return String.format(ReedMainUrl + urlExtension,
                getKeyWord(),
                getStartingSalary(),
                daysDict.get(Integer.valueOf(getHowManyDaysBack()))
        );
    }

    public static String PaginationUrl(String pageNo) {
        String urlExtension;
        if (isContract()) {
            urlExtension = "/jobs/%s-jobs?pageno=%s&salaryfrom=%s&contract=True&datecreatedoffset=%s";
        } else urlExtension = "/jobs/%s-jobs?pageno=%s&salaryfrom=%s&datecreatedoffset=%s";
        return String.format(ReedMainUrl + urlExtension,
                getKeyWord(),
                pageNo,
                getStartingSalary(),
                daysDict.get(Integer.valueOf(getHowManyDaysBack()))
        );
    }

    public static HashMap<Integer, String> daysDict = new HashMap<>();

    static {
        daysDict.put(1, "Today");
        daysDict.put(3, "LastThreeDays");
        daysDict.put(7, "LastWeek");
    }

    private static int resultsCount(Document doc) {
        return Integer.parseInt(doc.getElementsByClass(
                "col-sm-11 col-xs-12 page-title")
                .select("span.count").text().strip());
    }

    private static int getPagination(Document doc) {
        return UrlPagination.getPageCount(resultsCount(doc), results_per_page);
    }

    public static Document getDocument(String urlPerPage) throws IOException {
        return Jsoup.connect(urlPerPage).get();
    }

    public static void getReedJobs(String urlPerPage) throws IOException {
        // https://www.tutorialspoint.com/jsoup/jsoup_use_selector.htm
        Document doc = getDocument(urlPerPage);
        List<String> hrefs = doc.select("h3.title").select("a[href]").eachAttr("href");
        for (String href : hrefs) {
            dm.jobLink = ReedMainUrl + href;
            Document jobPage = new HttpConnection(dm.jobLink).parseHtmlData();
            dm.jobTitle = jobPage.getElementsByTag("h1").text();
            dm.advertiser = jobPage.getElementsByAttributeValue("itemprop", "name").select("span").text();
            dm.salary = jobPage.select("div.salary.col-xs-12.col-sm-6.col-md-6.col-lg-6").first().text();
            dm.location = jobPage.getElementsByAttributeValue("itemprop", "addressLocality").select("span").text();
            dm.jobType = jobPage.getElementsByAttributeValue("itemprop", "employmentType").select("span").text();

            DataModel.addToTable(dm.jobTitle, dm.salary, dm.location, dm.jobLink, dm.jobType, dm.advertiser);

        }
    }

    public static synchronized void runPagination() throws IOException {
        String url = reedBaseUrl();
        Document doc = Jsoup.connect(url).get();
        ArrayList<String> urls = DynamicReedUrls(doc);
        System.out.println(urls);
        if (urls.size() > 1) {
        for (String urlPerPage : urls) {
            getReedJobs(urlPerPage);
        }
    } else {
            getReedJobs(url);
        }
    }

    public static void main(String[] args) {  }


    @Override
    public void run() {
        try {
            runPagination();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
