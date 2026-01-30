package com.nexus.desktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8000/api/auth";
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public ApiService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    public Map<String, Object> login(String email, String password) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        HttpPost post = new HttpPost(BASE_URL + "/login");
        post.setHeader("Content-Type", "application/json");

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity, "UTF-8");

            if (response.getCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("user", jsonNode.get("user"));
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Login failed");
                return result;
            }
        } catch (ParseException e) {
            throw new IOException("Parse exception occurred", e);
        }
    }

    public boolean isLoggedIn() {
        // TODO: Check if we have a valid token/session
        return false;
    }

    public void logout() {
        // TODO: Clear tokens/sessions
    }
}