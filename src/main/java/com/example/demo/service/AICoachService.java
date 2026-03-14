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
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

@Service
public class AICoachService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AICoachService.class);

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAdvice(AICoachRequest request) {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "🎉 Chúc mừng! Bạn đã trả lời đúng tất cả các câu hỏi. Hãy tiếp tục phát huy!";
        }

        log.info("Bắt đầu xử lý generateAdvice với {} lỗi", mistakes.size());

        try {
            String prompt = buildPrompt(mistakes);
            log.debug("Prompt gửi đến AI: {}", prompt);
            String aiResponse = callAI(prompt);
            log.info("Gọi AI thành công, phản hồi nhận được (độ dài: {} ký tự)", aiResponse.length());
            return aiResponse;
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI: {}", e.getMessage(), e);
            log.debug("Cấu hình AI - URL: {}, Model: {}, Key: {}", apiUrl, model, apiKey != null ? "***" : "null");
            return buildSmartFallbackAdvice(mistakes);
        }
    }

    /**
     * Gọi AI trực tiếp, không fallback. Ném exception nếu lỗi.
     */
    public String callAIDirectly(AICoachRequest request) throws Exception {
        List<Mistake> mistakes = request.getMistakes();
        if (mistakes == null || mistakes.isEmpty()) {
            return "Không có lỗi để phân tích.";
        }
        String prompt = buildPrompt(mistakes);
        return callAI(prompt);
    }

    // Getters để controller lấy thông tin cấu hình
    public String getApiKey() { return apiKey; }
    public String getApiUrl() { return apiUrl; }
    public String getModel() { return model; }

    private String buildPrompt(List<Mistake> mistakes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là một gia sư AI thông minh, chuyên phân tích lỗi sai và đưa ra lời khuyên học tập. ");
        sb.append("Hãy xem xét các câu hỏi mà người học đã trả lời sai dưới đây, phân tích xem họ yếu ở mảng kiến thức nào, ");
        sb.append("và đề xuất phương pháp cải thiện cụ thể (bài đọc, video, bài tập, v.v.). Trả lời bằng tiếng Việt, thân thiện và chi tiết.\n\n");
        sb.append("📋 **DANH SÁCH CÂU SAI**\n");

        for (int i = 0; i < mistakes.size(); i++) {
            Mistake m = mistakes.get(i);
            sb.append("**Câu ").append(i + 1).append(":** ").append(m.getQuestion()).append("\n");
            sb.append("   - ❌ Đáp án của bạn: ").append(m.getUserAnswer()).append("\n");
            sb.append("   - ✅ Đáp án đúng: ").append(m.getCorrectAnswer()).append("\n");
            if (m.getExplanation() != null && !m.getExplanation().isEmpty()) {
                sb.append("   - 📘 Giải thích: ").append(m.getExplanation()).append("\n");
            }
            if (m.getExplanationLink() != null && !m.getExplanationLink().isEmpty()) {
                sb.append("   - 🔗 Bài viết tham khảo: ").append(m.getExplanationLink()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Dựa trên thông tin trên, hãy giúp tôi:\n");
        sb.append("1. **Phân tích** tôi đang yếu ở những chủ đề / mảng kiến thức nào?\n");
        sb.append("2. **Đề xuất** các nguồn học tập phù hợp (bài viết, sách, khóa học, video) – nếu có link cụ thể từ dữ liệu trên thì khuyến khích sử dụng.\n");
        sb.append("3. **Gợi ý lộ trình ôn tập** trong 1-2 tuần tới, bao gồm cả thời gian biểu gợi ý.\n");
        sb.append("Trả lời bằng tiếng Việt, văn phong gần gũi, có thể dùng emoji để sinh động.");

        return sb.toString();
    }

    private String callAI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "Bạn là một trợ lý AI chuyên về giáo dục, giúp sinh viên cải thiện kiến thức."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        log.info("Gửi request đến AI tại URL: {}", apiUrl);
        long startTime = System.currentTimeMillis();
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Nhận response từ AI sau {} ms, status code: {}", duration, response.getStatusCode());
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("AI trả về lỗi HTTP: {} - Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("AI API trả về lỗi: " + response.getStatusCode());
            }
            
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            log.debug("Nội dung AI: {}", content);
            return content;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Lỗi HTTP khi gọi AI: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
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