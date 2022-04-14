package io.github.ctlove0523.nsq.lookup;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class JdkNsqLookupClient implements NsqLookupClient {
    private String lookupEndpoint;
    private HttpClient httpClient;

    public JdkNsqLookupClient(String lookupEndpoint) {
        this.lookupEndpoint = lookupEndpoint;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5L))
                .build();
    }


    @Override
    public String getVersion() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/info"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            NsqVersion nsqVersion = new Gson().fromJson(response.body(), NsqVersion.class);
            return nsqVersion.getVersion();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean ping() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/ping"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().equals("OK");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public ProducerInfo getProducer(String topic) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/lookup?topic=" + topic))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), ProducerInfo.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
