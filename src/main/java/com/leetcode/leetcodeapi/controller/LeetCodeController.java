package com.leetcode.leetcodeapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.leetcode.leetcodeapi.models.SubmissionBody;
import com.leetcode.leetcodeapi.service.LeetCodeService;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Controller
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/leetcode")
public class LeetCodeController {
        private final String allowableCategories = "all, algorithms, database, shell, concurrency";
        private final String allowableDifficulties = "all, easy, medium, hard";

        @Autowired
        private LeetCodeService leetCodeService;

        @GetMapping("/questions")
        @ApiResponse(responseCode = "200", description = "Get all LeetCode questions", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getQuestions() {
                return leetCodeService.getQuestions();
        }

        @GetMapping("/questions/category/{category}")
        @ApiResponse(responseCode = "200", description = "Get LeetCode questions by category", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getQuestionsByCategory(
                        @PathVariable @ApiParam(value = "category of the questions", required = true, example = "array", allowableValues = allowableCategories) String category) {
                return leetCodeService.getFilteredQuestions(category, "all");
        }

        @GetMapping("/questions/difficulty/{difficulty}")
        @ApiResponse(responseCode = "200", description = "Get LeetCode questions by difficulty", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getQuestionsByDifficulty(
                        @PathVariable @ApiParam(value = "difficulty of the questions", required = true, example = "hard", allowableValues = allowableDifficulties) String difficulty) {
                return leetCodeService.getFilteredQuestions("all", difficulty);
        }

        @GetMapping("/questions/category/{category}/difficulty/{difficulty}")
        @ApiResponse(responseCode = "200", description = "Get LeetCode questions by category and difficulty", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getQuestionsByCategoryAndDifficulty(
                        @PathVariable @ApiParam(value = "category of the questions", required = true, example = "array", allowableValues = allowableCategories) String category,
                        @PathVariable @ApiParam(value = "difficulty of the questions", required = true, example = "hard", allowableValues = allowableDifficulties) String difficulty) {
                return leetCodeService.getFilteredQuestions(category, difficulty);
        }

        @GetMapping("/questions/{name}")
        @ApiResponse(responseCode = "200", description = "Get LeetCode question by name", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getQuestionById(
                        @PathVariable @ApiParam(value = "name / title-slug of the question", required = true, example = "two-sum") String name) {
                return leetCodeService.getQuestionByName(name, null);
        }

        @GetMapping("/questions/random")
        @ApiResponse(responseCode = "200", description = "Get random LeetCode question", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getRandomQuestion() {
                return leetCodeService.getRandomQuestion();
        }

        @GetMapping("/questions/category/{category}/difficulty/{difficulty}/random")
        @ApiResponse(responseCode = "200", description = "Get a random Leetcode question by category and difficulty", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getRandomQuestionByCategoryAndDifficulty(
                        @PathVariable @ApiParam(value = "category of the questions", required = true, example = "array", allowableValues = allowableCategories) String category,
                        @PathVariable @ApiParam(value = "difficulty of the questions", required = true, example = "hard", allowableValues = allowableDifficulties) String difficulty) {
                return leetCodeService.getRandomFilteredQuestion(category, difficulty);
        }

        @GetMapping("/questions/category/{category}/random")
        @ApiResponse(responseCode = "200", description = "Get a random Leetcode question by category", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getRandomQuestionByCategory(
                        @PathVariable @ApiParam(value = "category of the questions", required = true, example = "array", allowableValues = allowableCategories) String category) {
                return leetCodeService.getRandomFilteredQuestion(category, "all");
        }

        @GetMapping("/questions/difficulty/{difficulty}/random")
        @ApiResponse(responseCode = "200", description = "Get a random Leetcode question by difficulty", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> getRandomQuestionByDifficulty(
                        @PathVariable @ApiParam(value = "difficulty of the questions", required = true, example = "hard", allowableValues = allowableDifficulties) String difficulty) {
                return leetCodeService.getRandomFilteredQuestion("all", difficulty);
        }

        @PostMapping("/questions/{name}/submit")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
                        @Content(mediaType = "application/json", schema = @Schema(implementation = SubmissionBody.class)) })
        @ApiResponse(responseCode = "200", description = "Submit LeetCode question answer")
        public ResponseEntity<Object> submit(
                        @PathVariable @ApiParam(value = "name - title slug of the question name", required = true, example = "two-sum") String name,
                        @RequestBody @ApiParam(value = "submissionBody", required = true) SubmissionBody submissionBody,
                        // make the following two headers optional
                        @RequestHeader(value = "Session", required = false) @ApiParam(value = "LEETCODE_SESSION cookie", required = false) String leetCodeSession,
                        @RequestHeader(value = "X-CSRF-Token", required = false) @ApiParam(value = "X-CSRF-Token cookie", required = false) String csrfToken) {
                return leetCodeService.submit(name, submissionBody, leetCodeSession, csrfToken);
        }

        @GetMapping("/questions/submissions/{id}")
        @ApiResponse(responseCode = "200", description = "Get LeetCode question submission details", content = {
                        @Content(mediaType = "application/json") })
        public ResponseEntity<Object> submit(
                        @PathVariable String id,
                        @RequestHeader(value = "Session", required = false) @ApiParam(value = "LEETCODE_SESSION cookie", required = false) String leetCodeSession,
                        @RequestHeader(value = "X-CSRF-Token", required = false) @ApiParam(value = "X-CSRF-Token cookie", required = false) String csrfToken) {
                return leetCodeService.getSubmissions(id, leetCodeSession, csrfToken);
        }

        // TODO: Remove requiring the number of the question in the request for
        // submission
}
