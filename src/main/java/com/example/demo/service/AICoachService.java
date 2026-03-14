package com.example.demo.service;

import com.example.demo.dto.AICoachRequest;
import com.example.demo.dto.AICoachRequest.Mistake;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AICoachService {

    public String generateAdvice(AICoachRequest request) {
        List<Mistake> mistakes = request.getMistakes(); // ✅ gọi getMistakes()
        if (mistakes == null || mistakes.isEmpty()) {
            return "Chúc mừng! Bạn đã trả lời đúng tất cả các câu hỏi.";
        }

        StringBuilder advice = new StringBuilder("Dựa trên kết quả của bạn, tôi gợi ý ôn tập các nội dung sau:\n\n");

        for (Mistake m : mistakes) {
            advice.append("📌 ").append(m.getQuestion()).append("\n");
            advice.append("   ➤ Bạn đã chọn: ").append(m.getUserAnswer()).append("\n");
            advice.append("   ➤ Đáp án đúng: ").append(m.getCorrectAnswer()).append("\n");
            if (m.getExplanation() != null && !m.getExplanation().isEmpty()) {
                advice.append("   📘 ").append(m.getExplanation()).append("\n");
            }
            if (m.getExplanationLink() != null && !m.getExplanationLink().isEmpty()) {
                advice.append("   🔗 Xem thêm: ").append(m.getExplanationLink()).append("\n");
            }
            advice.append("\n");
        }

        advice.append("Hãy dành thời gian đọc lại các bài viết được đề cập để củng cố kiến thức nhé!");

        return advice.toString();
    }
}