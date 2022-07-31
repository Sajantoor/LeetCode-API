package com.leetcode.leetcodeapi.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;

@Service
public class LeetcodeService {
    private final String PROBLEM_API = "https://leetcode.com/api/problems/algorithms/";
    private final String GRAPHQL_API = "https://leetcode.com/graphql";

    private CloseableHttpClient client;
    private ObjectMapper mapper;

    public LeetcodeService() {
        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        this.mapper = new ObjectMapper();
    }

    public ResponseEntity<Object> getQuestions() {
        HttpGet request = new HttpGet(PROBLEM_API);

        try (CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                String message = response.getStatusLine().getReasonPhrase();
                return new ResponseEntity<Object>(message, HttpStatus.valueOf(statusCode));
            }

            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);
            JsonNode output = mapper.readTree(body);

            return new ResponseEntity<>(output, HttpStatus.OK);
        } catch (UnsupportedOperationException | IOException e) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public ResponseEntity<String> getQuestionByName(String name) {
        return ResponseEntity.ok("getQuestionById " + name);
    }

    public ResponseEntity<String> getQuestionByCategory(String category) {
        return ResponseEntity.ok("getQuestionByCategory");
    }

    public ResponseEntity<String> submit(String id) {
        return ResponseEntity.ok("submit");
    }
}
