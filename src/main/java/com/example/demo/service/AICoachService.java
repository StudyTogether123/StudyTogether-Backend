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

        sb.append("Dựa vào thông tin trên, hãy viết một lời khuyên học tập CHI TIẾT (khoảng 300-400 từ) bằng tiếng Việt, có cấu trúc rõ ràng. Lời khuyên cần bao gồm:\n");
        sb.append("1. **Điểm yếu chính**: Phân tích các lỗi sai, xác định chủ đề/kỹ năng cốt lõi mà học sinh còn yếu (ví dụ: kỹ năng teamwork, hiểu biết về AI, kỹ năng mềm, v.v.).\n");
        sb.append("2. **Điều cần lưu ý**: Nhắc lại ngắn gọn các khái niệm quan trọng liên quan đến từng câu sai, giải thích tại sao học sinh sai và hướng khắc phục tư duy.\n");
        sb.append("3. **Cách ôn tập**: Đề xuất các hành động cụ thể: đọc bài viết nào (dẫn link từ dữ liệu), làm bài tập gì, xem video nào, thực hành ra sao. Có thể gợi ý lộ trình ôn tập trong vài ngày tới.\n\n");
        sb.append("Kết thúc bằng một câu động viên ngắn gọn. Không mào đầu, không chào hỏi, viết thẳng vào nội dung chính.");

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
        generationConfig.put("temperature", 0.9);
        generationConfig.put("maxOutputTokens", 4000); // tăng lên 4000 để có nhiều chỗ cho nội dung dài
        generationConfig.put("topP", 0.95);
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
            return text;
        } catch (HttpClientErrorException e) {
            log.error("Lỗi HTTP khi gọi Gemini: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    private String buildDetailedFallbackAdvice(List<Mistake> mistakes) {
        Map<String, Integer> topicCount = new HashMap<>();
        for (Mistake m : mistakes) {
            String q = m.getQuestion().toLowerCase();
            if (q.contains("team") || q.contains("nhóm") || q.contains("làm việc nhóm")) {
                topicCount.put("Kỹ năng làm việc nhóm", topicCount.getOrDefault("Kỹ năng làm việc nhóm", 0) + 1);
            } else if (q.contains("ai") || q.contains("công nghệ") || q.contains("trí tuệ nhân tạo")) {
                topicCount.put("Hiểu biết về AI và công nghệ", topicCount.getOrDefault("Hiểu biết về AI và công nghệ", 0) + 1);
            } else if (q.contains("giao tiếp") || q.contains("văn hóa") || q.contains("cultural")) {
                topicCount.put("Giao tiếp và nhận thức văn hóa", topicCount.getOrDefault("Giao tiếp và nhận thức văn hóa", 0) + 1);
            } else if (q.contains("đàm phán") || q.contains("negotiation")) {
                topicCount.put("Kỹ năng đàm phán", topicCount.getOrDefault("Kỹ năng đàm phán", 0) + 1);
            } else if (q.contains("thời gian") || q.contains("time management")) {
                topicCount.put("Quản lý thời gian", topicCount.getOrDefault("Quản lý thời gian", 0) + 1);
            } else {
                topicCount.put("Kiến thức tổng quát", topicCount.getOrDefault("Kiến thức tổng quát", 0) + 1);
            }
        }

        String weakest = topicCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("kiến thức tổng quát");

        StringBuilder fallback = new StringBuilder();
        fallback.append("🧠 **Điểm yếu chính:**\n");
        fallback.append("Qua phân tích các câu sai, tôi nhận thấy bạn cần cải thiện nhiều nhất ở lĩnh vực **").append(weakest).append("**.\n\n");

        fallback.append("📌 **Điều cần lưu ý:**\n");
        if (weakest.contains("nhóm")) {
            fallback.append("Làm việc nhóm hiệu quả không chỉ là tập hợp người giỏi, mà là sự phối hợp, giao tiếp và tin tưởng lẫn nhau. Cần chú trọng đến giao tiếp rõ ràng và phân công vai trò phù hợp.\n\n");
        } else if (weakest.contains("AI")) {
            fallback.append("AI là công cụ hỗ trợ, không thể thay thế hoàn toàn con người trong các tình huống đòi hỏi cảm xúc, đạo đức và sự tinh tế. Hãy tập trung phát triển các kỹ năng mềm để làm việc hiệu quả với AI.\n\n");
        } else if (weakest.contains("giao tiếp")) {
            fallback.append("Giao tiếp hiệu quả không chỉ là truyền đạt thông tin mà còn là lắng nghe, thấu hiểu và điều chỉnh theo ngữ cảnh văn hóa. Cần rèn luyện kỹ năng này qua thực hành thường xuyên.\n\n");
        } else {
            fallback.append("Hãy xem lại các khái niệm cơ bản và thực hành nhiều hơn để nắm vững kiến thức.\n\n");
        }

        fallback.append("📚 **Cách ôn tập:**\n");
        fallback.append("- Đọc kỹ lại các bài viết được đề cập trong giải thích (nếu có).\n");
        fallback.append("- Tìm kiếm thêm tài liệu về chủ đề **").append(weakest).append("** trên Google hoặc YouTube.\n");
        fallback.append("- Thực hành với các tình huống thực tế, tham gia thảo luận nhóm để rèn luyện phản xạ.\n");
        fallback.append("- Làm lại các bài quiz tương tự để kiểm tra tiến bộ.\n\n");
        fallback.append("💪 Hãy kiên trì, bạn sẽ cải thiện nhanh chóng!");

        return fallback.toString();
    }
}