package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class IndeedJobs extends QueryBuilder implements Runnable, UrlPagination {

    static DataModel dm = new DataModel();
    static final int results_per_page = 50;
    private static final String IndeedUKBaseUrl = "https://www.indeed.co.uk";

    public static ArrayList<String> DynamicIndeedUKUrls(Document doc) {
        ArrayList<String> urlsByPage = new ArrayList<>();
        if (resultsCount(doc) < results_per_page) {
            return new ArrayList<>(Collections.singleton(BaseIndeedUKUrl()));
        } else for (int i = 0; i < getPagination(doc) * results_per_page; i += results_per_page) {
            urlsByPage.add(BaseIndeedUKUrl() + String.format("&start=%s", i));
        }
//        System.out.println(urlsByPage);
        return urlsByPage;
    }

    public static String BaseIndeedUKUrl() {
        String urlExtension;
        if (isContract()) {
            urlExtension = "/jobs?q=%s+£%s&sort=date&limit=50&fromage=%s&radius=25&jt=contract";
        } else urlExtension = "/jobs?q=%s+£%s&sort=date&limit=50&fromage=%s&radius=25";
        String indeedUKUrlPages = IndeedUKBaseUrl + urlExtension;
        return String.format(indeedUKUrlPages,
                getKeyWord(), getStartingSalary(), getHowManyDaysBack());
    }

    private static int resultsCount(Document doc) {
        int results_count = Integer.parseInt(
                doc.getElementById("searchCountPages").text().split(" ")[3]);
        System.out.println("Indeed results_count is : " + results_count);
        return results_count;
    }

    private static int getPagination(Document doc) {
        return UrlPagination.getPageCount(resultsCount(doc), results_per_page);
    }

    private static void splitLocationSalaryType(String[] job_meta_data) {
        if (job_meta_data.length == 3) {
            dm.jobType = job_meta_data[1];
            dm.salary = job_meta_data[2];
        } else if (job_meta_data.length <= 2) {
            for (String data : job_meta_data) {
                if (data.contains("£")) {
                    dm.salary = data;
                }
                if (data.contains("Contract") || data.contains("Permanent")) {
                    dm.jobType = data;
                }
            }
        }
    }

    public static Document getDocument(String urlPerPage) throws IOException {
        return Jsoup.connect(urlPerPage).get();
    }

    public static void getIndeedJobs(String urlPerPage) throws IOException {

        Document doc = getDocument(urlPerPage);
        Elements paragraphs = doc.getElementsByTag("h2");
        for (Element title : paragraphs) {
            dm.jobLink = IndeedUKBaseUrl + title.children().attr("href");
            Document jobPage = new HttpConnection(dm.jobLink).parseHtmlData();
            dm.jobTitle = jobPage.getElementsByTag("h3").text();
            dm.advertiser = jobPage.select(
                    "div.jobsearch-CompanyInfoWithoutHeaderImage")
                    .text().split("-")[0].strip();
            Elements jobHeaders = jobPage.getElementsByAttributeValue(
                    "class", "jobsearch-JobMetadataHeader icl-u-xs-mb--md");
            String[] job_meta_data = jobHeaders.select("span").attr(
                    "class", "jobsearch-JobMetadataHeader-iconLabel")
                    .text().split(" {2}");
            IndeedJobs.splitLocationSalaryType(job_meta_data);
            dm.location = jobPage.select(
                    "div.jobsearch-CompanyInfoWithoutHeaderImage")
                    .text().split("-")[1];
            DataModel.addToTable(dm.jobTitle, dm.salary, dm.location,
                    dm.jobLink, dm.jobType, dm.advertiser);
        }
    }

    public static synchronized void runPagination() throws IOException {
        String url = BaseIndeedUKUrl();
        Document doc = Jsoup.connect(url).get();
        ArrayList<String> urls = IndeedJobs.DynamicIndeedUKUrls(doc);
        System.out.println(urls);
        if (urls.size() > 1) {
            for (String urlPerPage : urls) {
                getIndeedJobs(urlPerPage);
            }
        } else {
            getIndeedJobs(url);
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

    public static void main(String[] args) { }

}
