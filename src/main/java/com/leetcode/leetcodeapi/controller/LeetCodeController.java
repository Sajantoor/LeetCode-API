package com.leetcode.leetcodeapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.leetcode.leetcodeapi.models.SubmissionBody;
import com.leetcode.leetcodeapi.service.LeetCodeService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Controller
@RequestMapping("/api/v1/leetcode")
public class LeetCodeController {

    @Autowired
    private LeetCodeService leetCodeService;

    @GetMapping("/questions")
    @ApiResponse(responseCode = "200", description = "Get all LeetCode questions", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getQuestions() {
        return leetCodeService.getQuestions();
    }

    @GetMapping("/questions/{name}")
    @ApiResponse(responseCode = "200", description = "Get LeetCode question by name", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getQuestionById(@PathVariable String name) {
        return leetCodeService.getQuestionByName(name);
    }

    @GetMapping("/questions/random")
    @ApiResponse(responseCode = "200", description = "Get random LeetCode question", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getRandomQuestion() {
        return leetCodeService.getRandomQuestion();
    }

    @GetMapping("/questions/category/{category}")
    @ApiResponse(responseCode = "200", description = "Get LeetCode question by category", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getQuestionByCategory(@PathVariable String category) {
        return leetCodeService.getQuestionsByCategory(category, "all");
    }

    @GetMapping("/questions/difficulty/{difficulty}")
    @ApiResponse(responseCode = "200", description = "Get LeetCode question by difficulty", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getQuestionByDifficulty(@PathVariable String difficulty) {
        return leetCodeService.getQuestionsByCategory("all", difficulty);
    }

    @GetMapping("/questions/category/{category}/difficulty/{difficulty}")
    @ApiResponse(responseCode = "200", description = "Get LeetCode question by category and difficulty", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> getQuestionByCategoryAndDifficulty(@PathVariable String category,
            @PathVariable String difficulty) {
        return leetCodeService.getQuestionsByCategory(category, difficulty);
    }

    @PostMapping("/questions/{name}/submit")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SubmissionBody.class)) })
    @ApiResponse(responseCode = "200", description = "Submit LeetCode question answer")
    public ResponseEntity<Object> submit(
            @PathVariable String name,
            @RequestBody SubmissionBody submissionBody,
            @RequestHeader("Session") String leetCodeSession,
            @RequestHeader("X-CSRF-Token") String csrfToken) {
        return leetCodeService.submit(name, submissionBody, leetCodeSession, csrfToken);
    }

    @GetMapping("/questions/submissions/{id}")
    @ApiResponse(responseCode = "200", description = "Get LeetCode question submission details", content = {
            @Content(mediaType = "application/json") })
    public ResponseEntity<Object> submit(
            @PathVariable String id,
            @RequestHeader("Session") String leetCodeSession,
            @RequestHeader("X-CSRF-Token") String csrfToken) {
        return leetCodeService.getSubmissions(id, leetCodeSession, csrfToken);
    }
}
