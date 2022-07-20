package com.leetcode.leetcodeapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.leetcode.leetcodeapi.service.LeetcodeService;

@Controller
@RequestMapping("/api/v1/leetcode")
public class LeetcodeController {

    @Autowired
    private LeetcodeService leetcodeService;

    // GET Leetcode questions
    @GetMapping("/questions")
    public ResponseEntity<String> getQuestions() {
        return leetcodeService.getQuestions();
    }

    // GET Leetcode question by id
    @GetMapping("/questions/{id}")
    public ResponseEntity<String> getQuestionById(@PathVariable String id) {
        return leetcodeService.getQuestionById(id);
    }

    // GET Leetcode Question by category
    @GetMapping("/questions/category/{category}")
    public ResponseEntity<String> getQuestionByCategory(@PathVariable String category) {
        return leetcodeService.getQuestionByCategory(category);
    }

    // Submit leetcode question answer
    @PostMapping("/questions/{id}/submit")
    public ResponseEntity<String> submit(@PathVariable String id) {
        return leetcodeService.submit(id);
    }
}
