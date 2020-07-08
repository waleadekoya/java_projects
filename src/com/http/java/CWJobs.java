package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CWJobs extends TotalJobs implements UrlPagination, Runnable {
    private static final String TotalJobMainUrl = "https://www.cwjobs.co.uk";
    static String resultCount = "CWJobs results_count is : ";

    public static String totalJobsBaseUrl() {
        String urlExtension;
        if (isContract()) {
            urlExtension = "/jobs/contract/%s?postedwithin=%s&salary=%s&salarytypeid=1";
        } else urlExtension = "/jobs/%s?postedwithin=%s&salary=%s&salarytypeid=1";
        String totalJobsBaseUrl = CWJobs.TotalJobMainUrl + urlExtension;
        return String.format(totalJobsBaseUrl,
                getKeyWord(), getHowManyDaysBack(), getStartingSalary()
        );
    }

    public static void getTotalJobs(String urlPerPage) throws IOException {
        Document doc = CWJobs.getDocument(urlPerPage);
        List<String> hrefs = doc.getElementsByTag("div")
                .select("div.job-title").select("a[href]").eachAttr("href");
        CWJobs.getJobData(hrefs);
    }

    public static int resultsCount(Document doc) throws NumberFormatException {
        return Integer.parseInt(doc.select(
                "div.page-title").tagName("span").text().split(" ")[0].strip().trim());
    }

    public static ArrayList<String> DynamicTotalJobsUrls(Document doc) throws IOException {
        Document docx = Jsoup.connect(CWJobs.totalJobsBaseUrl()).get();
        int resultsCount = resultsCount(doc);
        System.out.println(resultCount + resultsCount);
        ArrayList<String> urlsByPage = new ArrayList<>();
        if (resultsCount < results_per_page) {
            // if the number of results found is not more than a single page
            return new ArrayList<>(Collections.singleton(CWJobs.totalJobsBaseUrl()));
        } else {
            int pageCount = getPagination(docx);
            for (int i = 1; i < pageCount + 1; i++) {
                if (i == 1) {
                    urlsByPage.add(CWJobs.totalJobsBaseUrl());
                } else urlsByPage.add(CWJobs.totalJobsBaseUrl() + "&page=" + i);
            }
        }
        return urlsByPage;
    }


    public static synchronized void runPagination() throws IOException {
        String url = CWJobs.totalJobsBaseUrl();
        Document doc = CWJobs.getDocument(CWJobs.totalJobsBaseUrl());
        ArrayList<String> urls = CWJobs.DynamicTotalJobsUrls(doc);
        System.out.println("Run from CWJbs: " + urls);
        if (urls.size() > 1) {
            for (String urlPerPage : urls) {
                CWJobs.getTotalJobs(urlPerPage);
            }
        } else {
            CWJobs.getTotalJobs(url);
        }
    }
    



    @Override
    public void run() {
        try {
            runPagination();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
