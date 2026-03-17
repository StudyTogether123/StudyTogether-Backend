package com.example.demo.dto;

import java.time.LocalDateTime;

public record PostDTO(
        Long id,
        String title,
        String content,
        String author,
        String category,
        Boolean locked,
        Integer viewCount,
        LocalDateTime createdAt,
        String image
) {}