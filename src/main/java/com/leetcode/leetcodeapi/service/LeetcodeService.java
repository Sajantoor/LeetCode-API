package com.leetcode.leetcodeapi.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetcode.leetcodeapi.utilities.GraphQlQuery;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.HashMap;

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

    public ResponseEntity<Object> getQuestionByName(String name) {
        HttpPost request = new HttpPost(GRAPHQL_API);

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
        System.out.println(graphQlQuery.toString());
        StringEntity body = new StringEntity(graphQlQuery.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(body);

        // request.addHeader("Content-Type", "application/json");
        // request.addHeader("Accept", "application/json");
        // request.addHeader("Cache-Control", "no-cache");

        try (CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                String message = response.getStatusLine().getReasonPhrase();
                return new ResponseEntity<Object>(message, HttpStatus.valueOf(statusCode));
            }

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JsonNode output = mapper.readTree(responseBody);

            return new ResponseEntity<>(output, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public ResponseEntity<String> getQuestionByCategory(String category) {
        return ResponseEntity.ok("getQuestionByCategory");
    }

    public ResponseEntity<String> submit(String id) {
        return ResponseEntity.ok("submit");
    }
}
