package com.leetcode.leetcodeapi.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.leetcode.leetcodeapi.utilities.GraphQlQuery;
import com.leetcode.leetcodeapi.utilities.HttpRequestUtils;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.HashMap;

@Service
public class LeetcodeService {
    private final String PROBLEM_API = "https://leetcode.com/api/problems/%s/";
    private final String GRAPHQL_API = "https://leetcode.com/graphql";
    // TODO: include difficulty and category
    private final String[] CATEGORIES = { "all", "algorithms", "database", "shell", "concurrency" };

    private CloseableHttpClient client;

    public LeetcodeService() {
        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

    public ResponseEntity<Object> getQuestions() {
        return getQuestionsByCategory("all");
    }

    public ResponseEntity<Object> getQuestionByName(String name) {

        String query = """
                query getQuestionDetail($titleSlug: String!) {,
                    question(titleSlug: $titleSlug) {,
                        content,
                        stats,
                        codeDefinition,
                        sampleTestCase,
                        enableRunCode,
                        metaData,
                        translatedContent,
                    },
                }
                """;

        HashMap<String, String> variables = new HashMap<>();
        variables.put("titleSlug", name);
        GraphQlQuery graphQlQuery = new GraphQlQuery(query, variables);

        HttpPost request = new HttpPost(GRAPHQL_API);
        StringEntity body = graphQlQuery.getEntity();
        request.setEntity(body);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getQuestionsByCategory(String category) {
        if (!isValidCategory(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Category");
        }

        String url = String.format(PROBLEM_API, category);
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<String> submit(String id) {
        return ResponseEntity.ok("submit");
    }

    private boolean isValidCategory(String category) {
        category = category.toLowerCase();

        for (String str : this.CATEGORIES) {
            if (str.equals(category)) {
                return true;
            }
        }

        return false;
    }
}
