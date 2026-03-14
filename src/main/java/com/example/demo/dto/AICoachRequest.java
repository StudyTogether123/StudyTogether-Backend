package com.example.demo.dto;

import java.util.List;

public class AICoachRequest {
    private List<Mistake> mistakes;

    // Getter và Setter cho mistakes
    public List<Mistake> getMistakes() {
        return mistakes;
    }

    public void setMistakes(List<Mistake> mistakes) {
        this.mistakes = mistakes;
    }

    // Inner class Mistake
    public static class Mistake {
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private String explanation;
        private String explanationLink;

        // Getters và Setters
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public String getExplanationLink() {
            return explanationLink;
        }

        public void setExplanationLink(String explanationLink) {
            this.explanationLink = explanationLink;
        }
    }
}