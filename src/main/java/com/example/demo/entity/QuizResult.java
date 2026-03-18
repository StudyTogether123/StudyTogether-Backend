package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private int score;

    private int totalQuestions;

    private double percentage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT") // Sửa thành TEXT để lưu JSON dài
    private String answers; // Lưu dưới dạng JSON

    @Column(nullable = false)
    private boolean counted = true; // Thêm trường counted, mặc định true (lần đầu được tính điểm)

    public QuizResult() {}

    public QuizResult(Users user, Quiz quiz, int score, int totalQuestions, String answers) {
        this.user = user;
        this.quiz = quiz;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = totalQuestions > 0 ? (double) score / totalQuestions * 100 : 0;
        this.answers = answers;
        this.completedAt = LocalDateTime.now();
        this.counted = true; // mặc định là true, có thể set lại sau
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public boolean isCounted() { return counted; }
    public void setCounted(boolean counted) { this.counted = counted; }
}