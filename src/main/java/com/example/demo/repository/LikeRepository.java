package com.example.demo.repository;

import com.example.demo.entity.Like;
import com.example.demo.entity.Post;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(Users user, Post post);
    boolean existsByUserAndPost(Users user, Post post);
    Long countByPost(Post post);
    void deleteByUserAndPost(Users user, Post post);
}