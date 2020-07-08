package com.http.java;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpConnection {

    private final HttpURLConnection connection;
    String urlPath;

    public HttpConnection(String urlPath) throws IOException {
        this.urlPath = urlPath;
        URL url = new URL(urlPath);
//        System.out.println(url);
        this.connection = (HttpURLConnection) url.openConnection();
//        setRequestMethod();
//        setRequestProperty();
//        getResponseCode();
//        getHeaderFields();
//        readResponseData();
//        parseHtmlData();
    }

    public Document parseHtmlData() throws IOException {
        return Jsoup.connect(this.urlPath).get();
    }

    public void setRequestMethod() throws ProtocolException {
        this.connection.setRequestMethod("GET");
    }

    public void getResponseCode() throws IOException {
        System.out.println("Code: " + this.connection.getResponseCode());
        System.out.println(this.connection.getResponseMessage());
    }

    public String userAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36";
    }

    public void setRequestProperty() {
        this.connection.setRequestProperty("User-Agent", userAgent());
        this.connection.setRequestProperty("Content-Type", "application/json");
    }

    public void getHeaderFields() {
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String key : headers.keySet()) {
            System.out.println(key + ": " + headers.get(key));
        }
    }

    public void readResponseData() throws IOException {
        BufferedReader input = new BufferedReader(
                new InputStreamReader(this.connection.getInputStream())
        );
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
        input.close();
    }



}
