package com.example.demo.dto.request;

public record CreatePostRequest(
        String title,
        String content,
        String category,
        String type
) {}