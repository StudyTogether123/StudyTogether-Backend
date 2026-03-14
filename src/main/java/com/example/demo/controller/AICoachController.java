package com.example.demo.controller;

import com.example.demo.dto.AICoachRequest;
import com.example.demo.service.AICoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AICoachController {

    @Autowired
    private AICoachService aiCoachService;

    @PostMapping("/advice")
    public ResponseEntity<Map<String, String>> getAdvice(@RequestBody AICoachRequest request) {
        String advice = aiCoachService.generateAdvice(request);
        Map<String, String> response = new HashMap<>();
        response.put("advice", advice);
        return ResponseEntity.ok(response);
    }
}