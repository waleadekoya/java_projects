package com.http.java;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class HttpClientUtils {

    String urlPath;
    HttpRequest request;
    HttpClient client;
    HttpResponse<String> response;

    public HttpClientUtils(String urlPath) throws IOException, InterruptedException, ExecutionException {
        this.urlPath = urlPath;
        this.client = buildClient();
        this.request = buildRequest();
        this.response = sendAsyncRequest();
        System.out.println(this.response.body());
        sendRequestToFile();
    }

    public HttpClient buildClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(this.urlPath))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/html")
                .build();
    }

    public HttpResponse<String> sendRequest() throws IOException, InterruptedException {
        return this.client.send(this.request, HttpResponse.BodyHandlers.ofString());
    }

    public void sendRequestToFile() throws IOException, InterruptedException {
        this.client.send(this.request, HttpResponse.BodyHandlers.ofFile(Paths.get("body.txt")));
    }

    public HttpResponse<String> sendAsyncRequest() throws InterruptedException, ExecutionException {
        return this.client.sendAsync(this.request, HttpResponse.BodyHandlers.ofString()).get();
    }
}
