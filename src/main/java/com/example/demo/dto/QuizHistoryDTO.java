package com.example.demo.dto;

import java.time.LocalDateTime;

public class QuizHistoryDTO {
    private Long id;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private LocalDateTime completedAt;
    private boolean counted; // true: lần đầu được tính điểm, false: lần luyện tập

    public QuizHistoryDTO() {}

    public QuizHistoryDTO(Long id, String quizTitle, int score, int totalQuestions, LocalDateTime completedAt, boolean counted) {
        this.id = id;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = completedAt;
        this.counted = counted;
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isCounted() { return counted; }
    public void setCounted(boolean counted) { this.counted = counted; }
}