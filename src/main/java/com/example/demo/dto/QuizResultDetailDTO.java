package com.example.demo.dto;

import java.util.List;

public class QuizResultDetailDTO {
    private int score;
    private int totalQuestions;
    private int percentage;
    private int pointsEarned;
    private boolean counted;          // Thêm trường này
    private List<QuestionResultDTO> details;

    // Getters and Setters
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public boolean isCounted() { return counted; }
    public void setCounted(boolean counted) { this.counted = counted; }

    public List<QuestionResultDTO> getDetails() { return details; }
    public void setDetails(List<QuestionResultDTO> details) { this.details = details; }
}