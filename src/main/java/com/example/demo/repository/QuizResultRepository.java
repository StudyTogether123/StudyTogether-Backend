package com.example.demo.repository;

import com.example.demo.entity.QuizResult;
import com.example.demo.entity.Users;
import com.example.demo.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUser(Users user);

    List<QuizResult> findByQuiz(Quiz quiz);

    Optional<QuizResult> findByUserAndQuiz(Users user, Quiz quiz);

    // Sử dụng EntityGraph để fetch cả quiz (tránh LazyInitializationException)
    @EntityGraph(attributePaths = {"quiz"})
    List<QuizResult> findByUserOrderByCompletedAtDesc(Users user);

    Long countByQuiz(Quiz quiz);

    Long countByUser(Users user);
}