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
import org.json.JSONObject;
import com.leetcode.leetcodeapi.models.SubmissionBody;
import com.leetcode.leetcodeapi.utilities.GraphQlQuery;
import com.leetcode.leetcodeapi.utilities.HttpRequestUtils;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;

@Service
public class LeetCodeService {
    private final String PROBLEM_API = "https://leetcode.com/api/problems/%s/";
    private final String GRAPHQL_API = "https://leetcode.com/graphql";
    // %s is the problem name
    private final String SUBMISSION_API = "https://leetcode.com/problems/%s/submit/";
    // %s is the submission id
    private final String SUBMISSION_DETAILS_API = "https://leetcode.com/submissions/detail/%s/check";
    // TODO: include difficulty and category
    private final String[] CATEGORIES = { "all", "algorithms", "database", "shell", "concurrency" };
    private final String[] DIFFICULTIES = { "ALL", "EASY", "MEDIUM", "HARD" };

    private final CloseableHttpClient client;

    public LeetCodeService() {
        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

    public ResponseEntity<Object> getQuestions() {
        JsonNode response = getAllQuestions();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Object> getRandomQuestion() {
        JsonNode questions = getAllQuestions();
        int totalQuestions = questions.get("num_total").asInt();
        int randomIndex = (int) (Math.random() * totalQuestions);
        String randomQuestion = questions.get("stat_status_pairs").get(randomIndex).get("stat")
                .get("question__title_slug").asText();
        return getQuestionByName(randomQuestion);
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
        JSONObject variables = new JSONObject("{\"titleSlug\":\"" + name + "\"}");
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

    private JsonNode getAllQuestions() {
        String url = String.format(PROBLEM_API, "all");
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            return HttpRequestUtils.getJsonFromBody(response);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getQuestionsByCategory(String category, String difficulty) {
        category = category.toLowerCase();
        difficulty = difficulty.toUpperCase();

        if (!isValidCategory(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category");
        }

        if (!isValidDifficulty(difficulty)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid difficulty");
        }

        String query = """
                query problemsetQuestionList($categorySlug: String, $filters: QuestionListFilterInput) {
                    problemsetQuestionList: questionList(
                    categorySlug: $categorySlug
                    filters: $filters
                    ) {
                    total: totalNum
                    questions: data {
                        acRate
                        difficulty
                        freqBar
                        frontendQuestionId: questionFrontendId
                        isFavor
                        paidOnly: isPaidOnly
                        status
                        title
                        titleSlug
                        topicTags {
                        name
                        id
                        slug
                        }
                        hasSolution
                        hasVideoSolution
                    }
                    }
                }
                """;

        JSONObject variables = new JSONObject();

        if (category.equals("all")) {
            category = "";
        }

        if (difficulty.equals("ALL")) {
            difficulty = "";
        }

        variables.put("categorySlug", category);
        JSONObject difficultyJson = new JSONObject();
        difficultyJson.put("difficulty", difficulty);
        variables.put("filters", difficultyJson);

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

    private boolean isValidDifficulty(String difficulty) {
        for (String dif : DIFFICULTIES) {
            if (dif.equals(difficulty)) {
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<Object> submit(String name, SubmissionBody submissionBody, String leetCodeSession,
            String csrfToken) {
        String url = String.format(SUBMISSION_API, name);
        HttpPost request = new HttpPost(url);

        // Convert submissionBody to json string
        String submissionString;

        try {
            submissionString = HttpRequestUtils.convertModelToJsonString(submissionBody);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }

        StringEntity body = new StringEntity(submissionString, ContentType.APPLICATION_JSON);
        request.setEntity(body);
        authenticateRequest(request, leetCodeSession, csrfToken);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getSubmissions(String id, String leetCodeSession, String csrfToken) {
        String url = String.format(SUBMISSION_DETAILS_API, id);
        HttpGet request = new HttpGet(url);
        authenticateRequest(request, leetCodeSession, csrfToken);

        // Need to poll the submission status and then return the submission details
        int count = 0;

        final int MAX_POLL_COUNT = 10;
        while (count < MAX_POLL_COUNT) {
            try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
                JsonNode json = HttpRequestUtils.getJsonFromBody(response);
                if (json.get("state").asText().equals("PENDING")) {
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

        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Could not get submission details");
    }

    private void authenticateRequest(HttpRequestBase request, String leetcodeSession, String csrfToken) {
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
