package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TotalJobs extends QueryBuilder implements UrlPagination, Runnable {
    static DataModel dm = new DataModel();
    static final int results_per_page = 20;
    private static final String TotalJobMainUrl = "https://www.totaljobs.com";
    static String resultCount = "TotalJobs results_count is : ";

    public static void getTotalJobs(String urlPerPage) throws IOException {
        Document doc = getDocument(urlPerPage);
        List<String> hrefs = doc.getElementsByTag("div")
                .select("div.job-title").select("a[href]").eachAttr("href");
        getJobData(hrefs);
    }

    public static synchronized void getJobData(List<String> hrefs) throws IOException {
        System.out.println(hrefs);
        for (String href : hrefs) {
            dm.jobLink = href;
            Document jobPage = new HttpConnection(dm.jobLink).parseHtmlData();
            dm.jobTitle = jobPage.getElementsByTag("h1").text().trim();
            dm.advertiser = jobPage.getElementById("companyJobsLink").select("a").text();
            dm.salary = jobPage.select("li.salary.icon").select("div").text();
            String locationFirstMatch = jobPage.getElementsByClass(
                    "col-xs-12 col-sm-7 travelTime-locationText").select("div").text();
            String locationSecondMatch = jobPage.getElementsByClass("location icon")
                    .select("div").text();
            dm.location = (!locationSecondMatch.equals("")) ? locationSecondMatch : locationFirstMatch;
            dm.jobType = jobPage.getElementsByClass("job-type icon").select("div").text();
//            System.out.println(dm.jobLink + " " + dm.jobTitle);
            DataModel.addToTable(dm.jobTitle, dm.salary, dm.location,
                    dm.jobLink, dm.jobType, dm.advertiser);
        }

    }

    public static ArrayList<String> DynamicTotalJobsUrls(Document doc) throws IOException {
        Document docx = Jsoup.connect(totalJobsBaseUrl()).get();
        int resultsCount = resultsCount(doc);
        System.out.println(resultCount + resultsCount);
        ArrayList<String> urlsByPage = new ArrayList<>();
        if (resultsCount < results_per_page) {
            // if the number of results found is not more than a single page
            return new ArrayList<>(Collections.singleton(totalJobsBaseUrl()));
        } else {
            int pageCount = getPagination(docx);
            for (int i = 1; i < pageCount + 1; i++) {
                if (i == 1) {
                    urlsByPage.add(totalJobsBaseUrl());
                } else urlsByPage.add(totalJobsBaseUrl() + "&page=" + i);
            }
        }
        return urlsByPage;
    }

    public static String totalJobsBaseUrl() {
        String urlExtension;
        if (isContract()) {
            urlExtension = "/jobs/contract/%s?postedwithin=%s&salary=%s&salarytypeid=1";
        } else urlExtension = "/jobs/%s?postedwithin=%s&salary=%s&salarytypeid=1";
        String totalJobsBaseUrl = TotalJobMainUrl + urlExtension;
        return String.format(totalJobsBaseUrl,
                getKeyWord(), getHowManyDaysBack(), getStartingSalary()
        );
    }

    public static int resultsCount(Document doc) throws NumberFormatException {
        return Integer.parseInt(doc.select(
                "div.page-title")
                .tagName("span").text().split(" ")[0].strip().trim());
    }

    public static int getPagination(Document doc) {
        int resultsCount = resultsCount(doc);
        return UrlPagination.getPageCount(resultsCount, results_per_page);
    }

    public static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    public static synchronized void runPagination() throws IOException {
        String url = TotalJobs.totalJobsBaseUrl();
        Document doc = getDocument(url);
        ArrayList<String> urls = TotalJobs.DynamicTotalJobsUrls(doc);
        System.out.println(urls);
        if (urls.size() > 1) {
            for (String urlPerPage : urls) {
                getTotalJobs(urlPerPage);
            }
        } else {
            getTotalJobs(url);
        }
    }

    public static void main(String[] args) { }

    @Override
    public void run() {
        try {
            runPagination();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
