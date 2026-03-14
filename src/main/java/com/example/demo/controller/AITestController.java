package com.example.demo.controller;

import com.example.demo.dto.AICoachRequest;
import com.example.demo.dto.AICoachRequest.Mistake;
import com.example.demo.service.AICoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/test")
public class AITestController {

    @Autowired
    private AICoachService aiCoachService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testAI() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Tạo request giả với một câu hỏi mẫu
            List<Mistake> mistakes = new ArrayList<>();
            Mistake m = new Mistake();
            m.setQuestion("Câu hỏi test: 1+1 bằng mấy?");
            m.setUserAnswer("3");
            m.setCorrectAnswer("2");
            m.setExplanation("Phép tính đơn giản");
            m.setExplanationLink("https://example.com");
            mistakes.add(m);

            AICoachRequest request = new AICoachRequest();
            request.setMistakes(mistakes);

            // Gọi trực tiếp AI, không qua fallback
            String advice = aiCoachService.callAIDirectly(request);
            result.put("success", true);
            result.put("advice", advice);
            result.put("message", "Gọi AI thành công, đây là phản hồi thật từ AI");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            // Ghi stack trace dạng chuỗi
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            result.put("stacktrace", stackTrace.toString());

            // Thông tin cấu hình (ẩn key một phần)
            Map<String, Object> config = new HashMap<>();
            config.put("apiUrl", aiCoachService.getApiUrl());
            config.put("model", aiCoachService.getModel());
            String key = aiCoachService.getApiKey();
            config.put("apiKeySet", key != null && !key.isEmpty());
            if (key != null && key.length() > 8) {
                config.put("apiKeyPreview", key.substring(0, 5) + "..." + key.substring(key.length()-4));
            } else {
                config.put("apiKeyPreview", "invalid or too short");
            }
            result.put("config", config);
        }
        return ResponseEntity.ok(result);
    }
}