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

    @Override
    public Topics getTopics() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/topics"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), Topics.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return new Topics();
    }

    @Override
    public Channels getChannels(String topic) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/channels?topic=" + topic))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), Channels.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return new Channels();
    }

    @Override
    public Nodes listNodes() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(lookupEndpoint + "/nodes"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), Nodes.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return new Nodes();
    }

    @Override
    public boolean addTopic(String topic) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(lookupEndpoint + "/topic/create?topic=" + topic))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteTopic(String topic) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(lookupEndpoint + "/topic/delete?topic=" + topic))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean addChannel(String topic, String channel) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(lookupEndpoint + String.format("/channel/create?topic=%s&channel=%s", topic, channel)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteChannel(String topic, String channel) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(lookupEndpoint + String.format("/channel/delete?topic=%s&channel=%s", topic, channel)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}
