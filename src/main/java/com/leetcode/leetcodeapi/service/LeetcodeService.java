package com.leetcode.leetcodeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LeetcodeService {
    public ResponseEntity<String> getQuestions() {
        return ResponseEntity.ok("getQuestions");
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
