package com.example.demo.controller.publics;

import com.example.demo.dto.PostDTO;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.request.UpdatePostRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ========== PUBLIC: Lấy danh sách bài viết ==========
    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }

    // ========== PUBLIC: Lấy chi tiết bài viết ==========
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    // ========== CREATE: Chỉ admin mới được tạo (yêu cầu authentication) ==========
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName(); // Lấy username từ token
            PostDTO createdPost = postService.create(request, username);
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // ========== UPDATE: Chỉ admin hoặc tác giả mới được sửa ==========
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

    // ========== DELETE: Chỉ admin hoặc tác giả mới được xóa ==========
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
}