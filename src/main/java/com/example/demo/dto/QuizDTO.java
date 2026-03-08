package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private List<QuestionDTO> questions;
    private boolean active;
    private int totalQuestions;
    private int completedCount;

    public static class QuestionDTO {
        private Long id;
        private String content;
        private List<String> options;
        private String correctAnswer;

        // Constructors
        public QuestionDTO() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    }

    // Constructors
    public QuizDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getTotalQuestions() { return questions != null ? questions.size() : 0; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
}