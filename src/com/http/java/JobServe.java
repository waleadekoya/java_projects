package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class JobServe extends QueryBuilder implements Runnable {

    static DataModel dm = new DataModel();
    static final int results_per_page = 20;
    private static final String JobServeMainUrl = "https://www.jobserve.com";

    public static ArrayList<String> DynamicReedUrls(Document doc) {
        ArrayList<String> urlsByPage = new ArrayList<>();
        int resultsCount = resultsCount(doc);
        System.out.println("Reed results_count is : " + resultsCount);
        if (resultsCount < results_per_page) {
            System.out.println(jobServeBaseUrl());
            return new ArrayList<>(Collections.singleton(jobServeBaseUrl()));
        } else for (int i = 1; i < getPagination(doc) + 1; i++) {
            urlsByPage.add(PaginationUrl(String.valueOf(i)));
        }
        System.out.println(urlsByPage);
        return urlsByPage;
    }

    public static String jobServeBaseUrl() {
        String urlExtension = "/gb/en/JobListingBasic.aspx?shid=0D4D911E7B91D5FEE114";
        return JobServeMainUrl + urlExtension;
    }

    public static String PaginationUrl(String pageNo) {
        return jobServeBaseUrl() + String.format("&page=%s", pageNo);
    }

    public static Document getDocument(String urlPerPage) throws IOException {
        return Jsoup.connect(urlPerPage).get();
    }

    private static int resultsCount(Document doc) {
        return Integer.parseInt(doc.getElementsByClass("resultnumber")
                .select("span").text().strip());
    }

    private static int getPagination(Document doc) {
        return UrlPagination.getPageCount(resultsCount(doc), results_per_page);
    }

    public static void getJobServeJobs(String urlPerPage) throws IOException {
        Document doc = getDocument(urlPerPage);
        List<String> hrefs = doc.select("div.jobListHeaderPanel")
                .select("a[href$=\".jsjob\"]").eachAttr("href");
        Set<String> hrefs_deduplicated = new HashSet<>(hrefs);
        System.out.println(hrefs_deduplicated);
        for (String href : hrefs_deduplicated) {
            dm.jobLink = JobServeMainUrl + href;
            Document jobPage = getDocument(dm.jobLink);
            dm.jobTitle = jobPage.getElementById("positiontitle").select("h1").text();
            String advertiserTag = jobPage.getElementById("recruitername").select("span").text();
            Element salaryTag = jobPage.getElementById("md_rate");
            String locationTag = jobPage.getElementById("md_location").select("span").text();
            String jobTypeTag = jobPage.getElementById("td_job_type").select("span").text();
            dm.advertiser = (!advertiserTag.equals("")) ? advertiserTag : null;
            if (salaryTag != null) {
                dm.salary = salaryTag.select("span").text();
            } else dm.salary = null;
            dm.location = (!locationTag.equals("")) ? locationTag : null;
            dm.jobType = (!jobTypeTag.equals("")) ? jobTypeTag : null;

            DataModel.addToTable(dm.jobTitle, dm.salary, dm.location,
                    dm.jobLink, dm.jobType, dm.advertiser);

        }
    }

    public static synchronized void runPagination() throws IOException {
        String url = jobServeBaseUrl();
        Document doc = Jsoup.connect(url).get();
        System.out.println(getPagination(doc));
        ArrayList<String> urls = DynamicReedUrls(doc);
        System.out.println(urls);
        if (urls.size() > 1) {
            for (String urlPerPage : urls) {
                getJobServeJobs(urlPerPage);
                DataModel.saveTable(QueryBuilder.getKeyWord());
            }
        } else {
            getJobServeJobs(url);
        }
    }

    public void run() {
        try {
            runPagination();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Thread jobServe = new Thread(new JobServe());
        jobServe.start();
    }
}
