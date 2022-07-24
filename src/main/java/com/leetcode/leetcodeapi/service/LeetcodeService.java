package com.leetcode.leetcodeapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

@Service
public class LeetcodeService {
    private final String PROBLEM_API = "https://leetcode.com/api/problems/algorithms/";

    private HttpClient client;

    public LeetcodeService() {
        this.client = HttpClient.newHttpClient();
    }

    public ResponseEntity<Object> getQuestions() {
        URI uri = URI.create(PROBLEM_API);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        HttpStatus statusCode = HttpStatus.valueOf(response.statusCode());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode output;

        try {
            output = mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Object>(output, statusCode);
    }

    public ResponseEntity<String> getQuestionById(String id) {
        return ResponseEntity.ok("getQuestionById " + id);
    }

    public ResponseEntity<String> getQuestionByCategory(String category) {
        return ResponseEntity.ok("getQuestionByCategory");
    }

    public ResponseEntity<String> submit(String id) {
        return ResponseEntity.ok("submit");
    }
}
