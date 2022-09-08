package com.leetcode.leetcodeapi.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}
