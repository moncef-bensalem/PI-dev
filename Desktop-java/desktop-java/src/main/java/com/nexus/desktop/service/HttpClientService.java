package com.nexus.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpClientService {
    private static final String BASE_URL = "http://localhost:8000"; // Default Symfony dev server port
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpClientService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String post(String endpoint, Map<String, Object> data) throws IOException, InterruptedException {
        String jsonData = objectMapper.writeValueAsString(data);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(30)) // Add timeout
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String get(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(java.time.Duration.ofSeconds(30)) // Add timeout
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String put(String endpoint, Map<String, Object> data) throws IOException, InterruptedException {
        String jsonData = objectMapper.writeValueAsString(data);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(30)) // Add timeout
                .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String delete(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(java.time.Duration.ofSeconds(30)) // Add timeout
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}