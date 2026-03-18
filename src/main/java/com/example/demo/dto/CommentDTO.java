package com.example.demo.dto;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        String content,
        String author,
        Long userId,
        Long postId,
        LocalDateTime createdAt
) {}