package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByType(String type, Pageable pageable);
    Page<Post> findByTypeAndStatus(String type, PostStatus status, Pageable pageable);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
}