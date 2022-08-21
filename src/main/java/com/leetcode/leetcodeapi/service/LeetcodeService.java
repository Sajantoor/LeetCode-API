package com.leetcode.leetcodeapi.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.leetcode.leetcodeapi.models.SubmissionBody;
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
    // %s is the problem name
    private final String SUBMISSION_API = "https://leetcode.com/problems/%s/submit/";
    // %s is the submission id
    private final String SUBMISSION_DETAILS_API = "https://leetcode.com/submissions/detail/%s/check";
    // TODO: include difficulty and category
    private final String[] CATEGORIES = { "all", "algorithms", "database", "shell", "concurrency" };
    private final int MAX_POLL_COUNT = 10;

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

    public ResponseEntity<Object> getRandomQuestion() {
        JsonNode questions = getQuestionByCategory("all");
        int totalQuestions = questions.get("num_total").asInt();
        int randomIndex = (int) (Math.random() * totalQuestions);
        String randomQueston = questions.get("stat_status_pairs").get(randomIndex).get("stat")
                .get("question__title_slug").asText();
        return getQuestionByName(randomQueston);
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

    private JsonNode getQuestionByCategory(String category) {
        if (!isValidCategory(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Category");
        }

        String url = String.format(PROBLEM_API, category);
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            return json;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    // TOOD:: add difficulty and category
    public ResponseEntity<Object> getQuestionsByCategory(String category) {
        JsonNode json = getQuestionByCategory(category);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<Object> submit(String name, SubmissionBody submissionBody) {
        String url = String.format(SUBMISSION_API, name);
        HttpPost request = new HttpPost(url);

        // Convert submissionBody to json string
        String submissionString = "";

        try {
            submissionString = HttpRequestUtils.convertModelToJsonString(submissionBody);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }

        StringEntity body = new StringEntity(submissionString, ContentType.APPLICATION_JSON);
        request.setEntity(body);
        authenticateRequest(request);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getSubmissions(String id) {
        String url = String.format(SUBMISSION_DETAILS_API, id);
        HttpGet request = new HttpGet(url);
        authenticateRequest(request);

        // Need to poll the submission status and then return the submission details
        boolean result = false;
        int count = 0;

        while (!result && count < MAX_POLL_COUNT) {
            try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
                JsonNode json = HttpRequestUtils.getJsonFromBody(response);
                if (json.get("state").asText() == "PENDING") {
                    Thread.sleep(1000);
                } else {
                    return ResponseEntity.ok(json);
                }
            } catch (IOException | InterruptedException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error " + e.getMessage());
            }
            count++;
        }

        // TODO: Add better message for user
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    private void authenticateRequest(HttpRequestBase request) {
        String csrfToken = System.getenv("LEETCODE_CSRF_TOKEN");
        String leetcodeSession = System.getenv("LEETCODE_SESSION");
        String cookie = "LEETCODE_SESSION=" + leetcodeSession + "; csrftoken=" + csrfToken;

        request.addHeader("Cookie", cookie);
        request.addHeader("x-csrftoken", csrfToken);
        request.addHeader("Referer", request.getURI().toString());
        request.addHeader("Referrer-Policy", "strict-origin-when-cross-origin");
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
