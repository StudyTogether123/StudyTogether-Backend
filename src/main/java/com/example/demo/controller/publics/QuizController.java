package com.example.demo.controller.publics;

import com.example.demo.entity.Quiz;
import com.example.demo.entity.QuizResult;
import com.example.demo.entity.Users;
import com.example.demo.repository.QuizRepository;
import com.example.demo.repository.QuizResultRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.dto.QuizDTO;
import com.example.demo.dto.QuizHistoryDTO;
import com.example.demo.dto.QuizResultDetailDTO;
import com.example.demo.dto.request.SubmitQuizRequest;
import com.example.demo.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UsersRepository usersRepository;
    private final QuizService quizService;

    public QuizController(QuizRepository quizRepository,
                          QuizResultRepository quizResultRepository,
                          UsersRepository usersRepository,
                          QuizService quizService) {
        this.quizRepository = quizRepository;
        this.quizResultRepository = quizResultRepository;
        this.usersRepository = usersRepository;
        this.quizService = quizService;
    }

    @GetMapping("/daily")
    public ResponseEntity<QuizDTO> getDailyQuiz() {
        LocalDate today = LocalDate.now();
        log.info("🔍 GET /daily - Today's date: {}", today);

        Quiz quiz = quizRepository.findByDate(today).orElse(null);
        if (quiz == null) {
            log.warn("⚠️ No quiz found for date: {}", today);
            return ResponseEntity.notFound().build();
        }

        log.info("✅ Quiz found: id={}, title={}, active={}", quiz.getId(), quiz.getTitle(), quiz.isActive());
        return ResponseEntity.ok(convertToDTO(quiz, false));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        log.info("🔍 GET /{}", id);
        return quizRepository.findById(id)
                .map(quiz -> {
                    log.info("✅ Quiz found: id={}, title={}", quiz.getId(), quiz.getTitle());
                    return ResponseEntity.ok(convertToDTO(quiz, true));
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Quiz not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody SubmitQuizRequest request) {
        log.info("📝 POST /submit - quizId={}", request.getQuizId());

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
            request.setUserId(user.getId());

            QuizResultDetailDTO result = quizService.submitQuiz(request);
            log.info("✅ Quiz submitted successfully - userId={}, quizId={}, score={}/{}",
                    user.getId(), request.getQuizId(), result.getScore(), result.getTotalQuestions());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Error submitting quiz: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMyQuizHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("📜 GET /history - username: {}", username);

        return usersRepository.findByUsername(username)
                .map(user -> {
                    List<QuizResult> results = quizResultRepository.findByUserOrderByCompletedAtDesc(user);
                    List<QuizHistoryDTO> dtos = results.stream()
                            .map(r -> new QuizHistoryDTO(
                                    r.getId(),
                                    r.getQuiz() != null ? r.getQuiz().getTitle() : "Quiz",
                                    r.getScore(),
                                    r.getTotalQuestions(),
                                    r.getCompletedAt(),
                                    r.isCounted()
                            ))
                            .collect(Collectors.toList());
                    log.info("✅ Found {} history records for user: {}", dtos.size(), username);
                    return ResponseEntity.ok(dtos);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ User not found: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/results/{resultId}")
    public ResponseEntity<?> getQuizResult(@PathVariable Long resultId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("🔍 GET /results/{} - username: {}", resultId, username);

        return quizResultRepository.findById(resultId)
                .map(result -> {
                    if (!result.getUser().getUsername().equals(username)) {
                        log.warn("⛔ Unauthorized access to result {} by user {}", resultId, username);
                        return ResponseEntity.status(403).body("Không có quyền xem kết quả này");
                    }
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Quiz result not found with id: {}", resultId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserQuizStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("📊 GET /stats - username: {}", username);

        return usersRepository.findByUsername(username)
                .map(user -> {
                    List<QuizResult> results = quizResultRepository.findByUser(user);
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalQuizzes", results.size());
                    stats.put("averageScore", results.stream()
                            .mapToDouble(QuizResult::getPercentage)
                            .average()
                            .orElse(0));
                    stats.put("highestScore", results.stream()
                            .mapToDouble(QuizResult::getPercentage)
                            .max()
                            .orElse(0));
                    stats.put("totalPoints", user.getPoints());
                    log.info("✅ Stats computed for user: {}", username);
                    return ResponseEntity.ok(stats);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ User not found: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/leaderboard")
    public List<Map<String, Object>> getLeaderboard() {
        log.info("🏆 GET /leaderboard");
        List<Users> topUsers = usersRepository.findTop10ByOrderByPointsDesc();
        List<Map<String, Object>> result = topUsers.stream()
                .map(user -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("username", user.getUsername());
                    entry.put("fullName", user.getFullName());
                    entry.put("totalPoints", user.getPoints());
                    entry.put("quizCount", quizResultRepository.countByUser(user));
                    return entry;
                })
                .collect(Collectors.toList());
        log.info("✅ Leaderboard returned with {} entries", result.size());
        return result;
    }

    private QuizDTO convertToDTO(Quiz quiz, boolean includeAnswers) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setDate(quiz.getDate());
        dto.setActive(quiz.isActive());

        List<QuizDTO.QuestionDTO> questionDTOs = quiz.getQuestions().stream()
                .map(q -> {
                    QuizDTO.QuestionDTO qdto = new QuizDTO.QuestionDTO();
                    qdto.setId(q.getId());
                    qdto.setContent(q.getContent());
                    qdto.setOptions(q.getOptions());
                    if (includeAnswers) {
                        qdto.setCorrectAnswer(q.getCorrectAnswer());
                    }
                    return qdto;
                })
                .collect(Collectors.toList());
        dto.setQuestions(questionDTOs);
        return dto;
    }
}