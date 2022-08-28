package com.leetcode.leetcodeapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.leetcode.leetcodeapi.models.SubmissionBody;
import com.leetcode.leetcodeapi.service.LeetcodeService;

@Controller
@RequestMapping("/api/v1/leetcode")
public class LeetcodeController {

    @Autowired
    private LeetcodeService leetcodeService;

    // GET Leetcode questions
    @GetMapping("/questions")
    public ResponseEntity<Object> getQuestions() {
        return leetcodeService.getQuestions();
    }

    // GET Leetcode question by name
    @GetMapping("/questions/{name}")
    public ResponseEntity<Object> getQuestionById(@PathVariable String name) {
        return leetcodeService.getQuestionByName(name);
    }

    // GET random Leetcode question
    @GetMapping("/questions/random")
    public ResponseEntity<Object> getRandomQuestion() {
        return leetcodeService.getRandomQuestion();
    }

    // GET Leetcode Question by category
    @GetMapping("/questions/category/{category}/difficulty/{difficulty}")
    public ResponseEntity<Object> getQuestionByCategory(@PathVariable String category,
            @PathVariable String difficulty) {
        return leetcodeService.getQuestionsByCategory(category, difficulty);
    }

    // Submit leetcode question answer
    @PostMapping("/questions/{name}/submit")
    public ResponseEntity<Object> submit(@PathVariable String name, @RequestBody SubmissionBody submissionBody) {
        return leetcodeService.submit(name, submissionBody);
    }

    // Get leetcode question submission details
    @GetMapping("/questions/submissions/{id}")
    public ResponseEntity<Object> submit(@PathVariable String id) {
        return leetcodeService.getSubmissions(id);
    }
}
