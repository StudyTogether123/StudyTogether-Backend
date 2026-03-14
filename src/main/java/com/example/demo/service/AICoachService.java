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

    // ==================== Public Methods ====================

    public String generateAdvice(AICoachRequest request) {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "🎉 Chúc mừng! Bạn đã trả lời đúng tất cả các câu hỏi. Hãy tiếp tục phát huy!";
        }

        try {
            String prompt = buildPrompt(mistakes);
            return callAI(prompt);
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            return buildSmartFallbackAdvice(mistakes);
        }
    }

    public String callAIDirectly(AICoachRequest request) throws Exception {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "Không có lỗi để phân tích.";
        }
        String prompt = buildPrompt(mistakes);
        return callAI(prompt);
    }

    public String getApiKey() { return apiKey; }
    public String getApiUrl() { return apiUrl; }
    public String getModel() { return model; }

    // ==================== Private Methods ====================

    private String buildPrompt(List<Mistake> mistakes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là một gia sư AI chuyên nghiệp. Học sinh vừa làm quiz và có một số câu sai. Dưới đây là danh sách các câu hỏi sai, kèm đáp án đúng và giải thích (nếu có).\n\n");
        sb.append("📋 **DANH SÁCH CÂU SAI**\n");

        for (int i = 0; i < mistakes.size(); i++) {
            Mistake m = mistakes.get(i);
            sb.append("**Câu ").append(i + 1).append(":** ").append(m.getQuestion()).append("\n");
            sb.append("   - ❌ Câu trả lời của bạn: ").append(m.getUserAnswer()).append("\n");
            sb.append("   - ✅ Đáp án đúng: ").append(m.getCorrectAnswer()).append("\n");
            if (m.getExplanation() != null && !m.getExplanation().isEmpty()) {
                sb.append("   - 📘 Giải thích: ").append(m.getExplanation()).append("\n");
            }
            if (m.getExplanationLink() != null && !m.getExplanationLink().isEmpty()) {
                sb.append("   - 🔗 Bài viết tham khảo: ").append(m.getExplanationLink()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Dựa vào thông tin trên, hãy viết một lời khuyên học tập ngắn gọn nhưng chi tiết (khoảng 300-400 từ) bằng tiếng Việt, đi thẳng vào vấn đề, không dài dòng. Lời khuyên cần:\n");
        sb.append("1. **Phân tích điểm yếu**: Xác định các chủ đề/kỹ năng mà học sinh còn yếu (ví dụ: đàm phán, quản lý thời gian, BATNA,...).\n");
        sb.append("2. **Nhắc lại ngắn gọn khái niệm quan trọng** liên quan đến từng câu sai.\n");
        sb.append("3. **Đề xuất hành động cụ thể**: Nên đọc bài viết nào (dẫn link từ dữ liệu), làm bài tập gì, xem video nào, thực hành ra sao.\n");
        sb.append("4. **Gợi ý lộ trình ôn tập nhanh trong vài ngày tới**.\n");
        sb.append("5. **Kết thúc bằng câu động viên ngắn gọn**.\n\n");
        sb.append("Không mào đầu, không chào hỏi, không giải thích dài dòng. Bắt đầu ngay bằng phần phân tích.");

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
        generationConfig.put("temperature", 0.8);
        generationConfig.put("maxOutputTokens", 2500); // tăng lên 2500 để đảm bảo đủ dài
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
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (HttpClientErrorException e) {
            log.error("Lỗi HTTP khi gọi Gemini: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    private String buildSmartFallbackAdvice(List<Mistake> mistakes) {
        log.info("Sử dụng fallback thông minh do lỗi kết nối AI");
        Map<String, Integer> topicCount = new HashMap<>();
        for (Mistake m : mistakes) {
            String question = m.getQuestion().toLowerCase();
            if (question.contains("đàm phán") || question.contains("negotiation")) {
                topicCount.put("Đàm phán", topicCount.getOrDefault("Đàm phán", 0) + 1);
            } else if (question.contains("thời gian") || question.contains("time management")) {
                topicCount.put("Quản lý thời gian", topicCount.getOrDefault("Quản lý thời gian", 0) + 1);
            } else if (question.contains("batna")) {
                topicCount.put("BATNA", topicCount.getOrDefault("BATNA", 0) + 1);
            } else {
                topicCount.put("Kiến thức chung", topicCount.getOrDefault("Kiến thức chung", 0) + 1);
            }
        }

        String weakestTopic = topicCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Kiến thức tổng quát");

        StringBuilder advice = new StringBuilder();
        advice.append("🧠 **Phân tích điểm yếu của bạn**\n\n");
        advice.append("Dựa trên các câu sai, tôi nhận thấy bạn cần cải thiện nhiều nhất ở mảng **").append(weakestTopic).append("**.\n\n");
        advice.append("📚 **Các chủ đề cần ôn tập:**\n");
        for (Map.Entry<String, Integer> entry : topicCount.entrySet()) {
            advice.append("   - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" lỗi\n");
        }
        advice.append("\n");
        advice.append("🔍 **Phương pháp cải thiện:**\n");
        advice.append("   - Đọc kỹ lại các bài viết được đề cập dưới đây.\n");
        advice.append("   - Ghi chép lại những khái niệm quan trọng (ví dụ: BATNA, phân biệt position và interest).\n");
        advice.append("   - Thực hành với các tình huống giả định để áp dụng lý thuyết.\n\n");
        advice.append("📖 **Danh sách bài viết tham khảo:**\n");
        for (Mistake m : mistakes) {
            if (m.getExplanationLink() != null && !m.getExplanationLink().isEmpty()) {
                advice.append("   - ").append(m.getExplanationLink()).append(" : ").append(m.getQuestion()).append("\n");
            }
        }
        advice.append("\n");
        advice.append("💡 **Lộ trình ôn tập đề xuất (1 tuần):**\n");
        advice.append("   - Ngày 1-2: Đọc và hiểu các bài viết về ").append(weakestTopic).append(".\n");
        advice.append("   - Ngày 3-4: Làm bài tập trắc nghiệm và tự luận liên quan.\n");
        advice.append("   - Ngày 5: Ôn tập lại toàn bộ và tự giải thích lại các khái niệm.\n");
        advice.append("   - Ngày 6-7: Tham gia thảo luận nhóm hoặc viết blog tóm tắt kiến thức.\n\n");
        advice.append("Nếu có thể, hãy kết nối lại AI để nhận lời khuyên chi tiết hơn nhé!");
        return advice.toString();
    }
}