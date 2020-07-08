package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;


public class CVLibrary extends QueryBuilder implements Runnable {

    static DataModel dm = new DataModel();
    static final int results_per_page = 50;
    private static final String cvLibraryMainUrl = "https://www.cv-library.co.uk";

    public static ArrayList<String> DynamicReedUrls(Document doc) {
        ArrayList<String> urlsByPage = new ArrayList<>();
        int resultsCount = resultsCount(doc);
        System.out.println("CV Library results_count is : " + resultsCount);
        if (resultsCount < results_per_page) {
            return new ArrayList<>(Collections.singleton(cvLibraryBaseUrl()));
        } else for (int i = 1; i < getPagination(doc) + 1; i++) {
            urlsByPage.add(PaginationUrl(String.valueOf(i)));
        }
        return urlsByPage;
    }

    public static String cvLibraryBaseUrl() {
        String urlExtension = "/%s-jobs?contract=%s&perpage=%s&posted=%s&salarymin=%s";
        return cvLibraryMainUrl + String.format(urlExtension,
                getKeyWord(), isContract(), results_per_page,
                getHowManyDaysBack(), getStartingSalary());
    }

    public static String PaginationUrl(String pageNo) {
        return cvLibraryBaseUrl() +  String.format("&page=%s", pageNo);
    }

    public static Document getDocument(String urlPerPage) throws IOException {
        return Jsoup.connect(urlPerPage).get();
    }

    private static int resultsCount(Document doc) {
        String[] resultText = doc.getElementsByClass("search-header__results")
                .select("p").text().split(" ");
        return Integer.parseInt(resultText[resultText.length - 2]);
    }

    private static int getPagination(Document doc) {
        return UrlPagination.getPageCount(resultsCount(doc), results_per_page);
    }

    public static synchronized void getCVLibraryJobs(String urlPerPage) throws IOException {
        List<String> hrefs = getDocument(urlPerPage).getElementsByClass(
                "job__title")
                .select("a[href]").eachAttr("href");
        for (String href : hrefs) {
            if (href.toLowerCase().contains("trainee")) {continue;}
            dm.jobLink = cvLibraryMainUrl + href;
            Document jobPage = getDocument(dm.jobLink);
            dm.jobTitle = jobPage.getElementsByClass("job__title")
                    .select("h1").text();
            List<String> advertiserTag = jobPage.getElementsByClass(
                    "job__header-posted sm-no").select("span").eachText();
            if (advertiserTag.size() == 2) {
                dm.advertiser = advertiserTag.get(1);
            } else if (advertiserTag.size() == 0) {
                List<String> dataContainer = jobPage.getElementsByClass(
                        "job__details-value").tagName("a").eachText();
                dm.advertiser = dataContainer.get(3);
//                dm.jobType = dataContainer.get(4);
                dm.location = dataContainer.get(1);
            }
            List<String> locationSalaryAdvertiserContainer = jobPage
                    .select("div.job__header-info").select("dl")
                    .select("dd").eachText();
            if (locationSalaryAdvertiserContainer.size() > 2) {
                dm.location = locationSalaryAdvertiserContainer.get(0);
                if (locationSalaryAdvertiserContainer.get(1).contains("£")) {
                    dm.salary = locationSalaryAdvertiserContainer.get(1);
                } else dm.salary = null;
                dm.advertiser = locationSalaryAdvertiserContainer.get(
                        locationSalaryAdvertiserContainer.size() - 1
                );
            }
            List<String> jobTypeContainer = jobPage.getElementsByClass(
                    "job__details-value").tagName("dl")
                    .tagName("div").tagName("dd").eachText();

            if (jobTypeContainer.get(4).contains("Permanent")
                    || jobTypeContainer.get(4).contains("Contract")) {
                dm.jobType = jobTypeContainer.get(4);
            } else if (jobTypeContainer.get(3).contains("Permanent")
                    || jobTypeContainer.get(3).contains("Contract")) {
                dm.jobType = jobTypeContainer.get(3);
            } else dm.jobType = null;

            DataModel.addToTable(dm.jobTitle, dm.salary, dm.location,
                    dm.jobLink, dm.jobType, dm.advertiser);

        }
    }

    public static synchronized void runPagination() throws IOException {
        String url = cvLibraryBaseUrl();
        Document doc = Jsoup.connect(url).get();
        ArrayList<String> urls = DynamicReedUrls(doc);
        System.out.println(urls);
        if (urls.size() > 1) {
            for (String urlPerPage : urls) {
                getCVLibraryJobs(urlPerPage);
            }
        } else {
            getCVLibraryJobs(url);
        }
    }

    public void run() {
        try {
            runPagination();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { }
}
