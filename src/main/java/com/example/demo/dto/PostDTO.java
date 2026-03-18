package com.example.demo.dto;

import java.time.LocalDateTime;
import com.example.demo.entity.PostStatus;

public record PostDTO(
        Long id,
        String title,
        String content,
        String author,
        String category,
        Boolean locked,
        Integer viewCount,
        LocalDateTime createdAt,
        String image,
        Long likeCount,
        Long commentCount,
        String type,
        PostStatus status
) {}