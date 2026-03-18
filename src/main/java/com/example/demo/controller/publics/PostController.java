package com.example.demo.controller.publics;

import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.request.CreateCommentRequest;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.request.UpdatePostRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    // ========== LẤY TẤT CẢ BÀI VIẾT (có thể bỏ nếu không cần) ==========
    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }

    // ========== LẤY BÀI VIẾT KIẾN THỨC (ARTICLE) ==========
    @GetMapping("/knowledge")
    public Page<PostDTO> getKnowledgePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postService.getPostsByType("article", pageable);
    }

    // ========== LẤY BÀI VIẾT CỘNG ĐỒNG (COMMUNITY) ==========
    @GetMapping("/community")
    public Page<PostDTO> getCommunityPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postService.getPostsByType("community", pageable);
    }

    // ========== LẤY CHI TIẾT BÀI VIẾT ==========
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    // ========== TẠO BÀI VIẾT MỚI (yêu cầu đăng nhập) ==========
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            PostDTO createdPost = postService.create(request, username);
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== CẬP NHẬT BÀI VIẾT ==========
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestBody UpdatePostRequest request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            PostDTO updatedPost = postService.update(id, request, username);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== XÓA BÀI VIẾT ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            postService.deletePost(id, username);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa bài viết thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== LIKE / UNLIKE BÀI VIẾT ==========
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean liked = postService.toggleLike(id, username);
            Map<String, Boolean> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== KIỂM TRA TRẠNG THÁI LIKE ==========
    @GetMapping("/{id}/like/status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long id,
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean liked = postService.isLikedByUser(id, username);
            Map<String, Boolean> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== LẤY DANH SÁCH BÌNH LUẬN ==========
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long id) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(id);
        return ResponseEntity.ok(comments);
    }

    // ========== THÊM BÌNH LUẬN ==========
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody CreateCommentRequest request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            CommentDTO comment = commentService.addComment(id, username, request.content());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}