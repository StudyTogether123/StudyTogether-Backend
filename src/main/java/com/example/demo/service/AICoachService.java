package com.example.demo.service;

import com.example.demo.dto.AICoachRequest;
import com.example.demo.dto.AICoachRequest.Mistake;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Service
public class AICoachService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AICoachService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAdvice(AICoachRequest request) {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "🎉 Chúc mừng! Bạn đã trả lời đúng tất cả các câu hỏi. Hãy tiếp tục phát huy!";
        }

        try {
            String prompt = buildDetailedPrompt(mistakes);
            return callAI(prompt);
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            return buildDetailedFallbackAdvice(mistakes);
        }
    }

    public String callAIDirectly(AICoachRequest request) throws Exception {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "Không có lỗi để phân tích.";
        }
        String prompt = buildDetailedPrompt(mistakes);
        return callAI(prompt);
    }

    public String getApiKey() { return apiKey; }
    public String getApiUrl() { return apiUrl; }
    public String getModel() { return model; }

    // ==================== Private Methods ====================

    private String buildDetailedPrompt(List<Mistake> mistakes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là gia sư AI. Học sinh đã sai các câu sau (kèm đáp án đúng):\n\n");
        for (Mistake m : mistakes) {
            sb.append("- Câu hỏi: ").append(m.getQuestion()).append("\n");
            sb.append("  Đáp án đúng: ").append(m.getCorrectAnswer()).append("\n");
            if (m.getExplanation() != null && !m.getExplanation().isEmpty()) {
                sb.append("  Giải thích: ").append(m.getExplanation()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("Hãy viết một lời khuyên bằng tiếng Việt, dài khoảng 150-200 từ, có cấu trúc rõ ràng gồm 3 phần:\n");
        sb.append("1. **Điểm yếu chính**: Phân tích xem học sinh yếu ở mảng kiến thức/kỹ năng nào (ví dụ: teamwork, hiểu về AI, giao tiếp, v.v.).\n");
        sb.append("2. **Điều cần lưu ý**: Nhắc lại ngắn gọn những khái niệm quan trọng liên quan.\n");
        sb.append("3. **Cách ôn tập**: Đề xuất 2-3 hành động cụ thể (đọc bài viết, thực hành, xem video,...).\n");
        sb.append("Không chào hỏi, không mào đầu. Trình bày rõ ràng từng phần.\n");
        return sb.toString();
    }

    private String callAI(String prompt) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 800); // đủ cho 150-200 từ tiếng Việt
        generationConfig.put("topP", 0.9);
        generationConfig.put("topK", 40);
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Gửi request đến Gemini API tại URL: {}", apiUrl);
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Nhận response từ Gemini sau {} ms, status code: {}", duration, response.getStatusCode());

            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            log.info("Gemini response text length: {}", text.length());
            log.debug("Gemini response preview: {}", text.substring(0, Math.min(200, text.length())));
            return text;
        } catch (HttpClientErrorException e) {
            log.error("Lỗi HTTP khi gọi Gemini: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    private String buildDetailedFallbackAdvice(List<Mistake> mistakes) {
        // Phân tích sơ bộ để tìm điểm yếu chính
        Map<String, Integer> topicCount = new HashMap<>();
        for (Mistake m : mistakes) {
            String q = m.getQuestion().toLowerCase();
            if (q.contains("team") || q.contains("nhóm") || q.contains("phối hợp") || q.contains("giao tiếp")) {
                topicCount.put("kỹ năng làm việc nhóm và giao tiếp", topicCount.getOrDefault("kỹ năng làm việc nhóm và giao tiếp", 0) + 1);
            } else if (q.contains("ai") || q.contains("công nghệ") || q.contains("dữ liệu")) {
                topicCount.put("vai trò của AI và công nghệ", topicCount.getOrDefault("vai trò của AI và công nghệ", 0) + 1);
            } else if (q.contains("văn hóa") || q.contains("đa văn hóa") || q.contains("quốc tế")) {
                topicCount.put("giao tiếp đa văn hóa", topicCount.getOrDefault("giao tiếp đa văn hóa", 0) + 1);
            } else {
                topicCount.put("kiến thức nền tảng", topicCount.getOrDefault("kiến thức nền tảng", 0) + 1);
            }
        }

        String weakest = topicCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("kiến thức tổng quát");

        // Tạo fallback với cấu trúc yêu cầu
        StringBuilder advice = new StringBuilder();
        advice.append("**Điểm yếu chính:** Bạn còn yếu ở mảng **").append(weakest).append("**.\n\n");
        advice.append("**Điều cần lưu ý:** Hãy nhớ rằng thành công không chỉ đến từ cá nhân mà cần sự phối hợp nhóm, và AI chỉ là công cụ hỗ trợ, không thể thay thế kỹ năng con người.\n\n");
        advice.append("**Cách ôn tập:**\n");
        advice.append("- Đọc lại các bài viết liên quan đến ").append(weakest).append(".\n");
        advice.append("- Thực hành qua các tình huống thực tế, thảo luận nhóm.\n");
        advice.append("- Xem video về kỹ năng mềm và ứng dụng AI.\n");
        return advice.toString();
    }
}