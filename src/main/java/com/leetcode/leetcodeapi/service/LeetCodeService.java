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
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        String randomQuestion = questions.get("stat_status_pairs")
                .get(randomIndex).get("stat")
                .get("question__title_slug")
                .asText();
        Integer randomQuestionId = questions.get("stat_status_pairs")
                .get(randomIndex).get("stat")
                .get("frontend_question_id")
                .asInt();

        // check if the question is paid only
        boolean paidOnly = questions.get("stat_status_pairs")
                .get(randomIndex).get("paid_only")
                .asBoolean();

        if (paidOnly) {
            return getRandomQuestion();
        }

        return getQuestionByName(randomQuestion, randomQuestionId);
    }

    public ResponseEntity<Object> getRandomFilteredQuestion(String category, String difficulty) {
        // TODO: There is only 50 questions per page, so this request only returns 50
        // questions
        // This is fine for now but in the future we should fix this by either making
        // multiple questions
        // or storing the questions in a databaes and then querying the database
        JsonNode questions = getFilteredQuestionsJSON(category, difficulty);
        int totalQuestions = questions.get("questions").size();
        int randomIndex = (int) (Math.random() * totalQuestions);

        // check if there are no questions
        if (totalQuestions == 0) {
            return ResponseEntity.ok(questions);
        }

        String randomQuestion = questions.get("questions").get(randomIndex)
                .get("titleSlug")
                .asText();

        Integer questionID = questions.get("questions").get(randomIndex)
                .get("frontendQuestionId")
                .asInt();

        // check if the question is paid only
        boolean paidOnly = questions.get("questions").get(randomIndex)
                .get("paidOnly")
                .asBoolean();

        if (paidOnly) {
            return getRandomFilteredQuestion(category, difficulty);
        }

        return getQuestionByName(randomQuestion, questionID);
    }

    public ResponseEntity<Object> getQuestionByName(String name, Integer id) {
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
            JsonNode responseJson = HttpRequestUtils.getJsonFromBody(response);

            // response has form {"data": {"question": { ... }}} which is not a nice format
            // We want to return {{ ... }}
            responseJson = responseJson.get("data").get("question");

            // JsonNode is immutable, so we need to make a copy of it to a ObjectNode which
            // is mutable
            ObjectNode json = responseJson.deepCopy();

            // stats, codeDefinition, metaData are all json strings, so we need to convert
            // them to JSON objects
            String stats = json.get("stats").textValue();
            String codeDefinition = json.get("codeDefinition").textValue();
            String metaData = json.get("metaData").textValue();

            JsonNode statsJson = HttpRequestUtils.getJsonFromString(stats);
            JsonNode codeDefinitionJson = HttpRequestUtils.getJsonFromString(codeDefinition);
            JsonNode metaDataJson = HttpRequestUtils.getJsonFromString(metaData);

            json.put("titleSlug", name);

            if (id != null) {
                json.put("id", id);
            }

            json.set("stats", statsJson);
            json.set("codeDefinition", codeDefinitionJson);
            json.set("metaData", metaDataJson);
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

    private JsonNode getFilteredQuestionsJSON(String category, String difficulty) {
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

        if (!difficulty.equals("")) {
            JSONObject difficultyJson = new JSONObject();
            difficultyJson.put("difficulty", difficulty);
            variables.put("filters", difficultyJson);
        } else {
            // Set to empty object if difficulty is empty
            variables.put("filters", new JSONObject());
        }

        GraphQlQuery graphQlQuery = new GraphQlQuery(query, variables);

        HttpPost request = new HttpPost(GRAPHQL_API);
        StringEntity body = graphQlQuery.getEntity();
        request.setEntity(body);

        try (CloseableHttpResponse response = HttpRequestUtils.makeHttpRequest(request, client)) {
            JsonNode json = HttpRequestUtils.getJsonFromBody(response);
            json = json.get("data").get("problemsetQuestionList");
            return json;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getFilteredQuestions(String category, String difficulty) {
        JsonNode json = getFilteredQuestionsJSON(category, difficulty);
        return ResponseEntity.ok(json);
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
        // fall back to env variables
        if (leetcodeSession == null || csrfToken == null) {
            leetcodeSession = System.getenv("LEETCODE_SESSION");
            csrfToken = System.getenv("LEETCODE_CSRF_TOKEN");
        }

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
