package com.example.demo.controller.admin;

import com.example.demo.dto.PostDTO;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.request.UpdatePostRequest;
import com.example.demo.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        System.out.println("AdminPostController.getAllPosts called");
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        System.out.println("AdminPostController.getPostById called with id: " + id);
        PostDTO post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequest request,
                                              Authentication authentication) {
        System.out.println("AdminPostController.createPost called by user: " + authentication.getName());
        String username = authentication.getName();

        // Nếu type không được gửi lên, mặc định là "article"
        if (request.type() == null || request.type().isBlank()) {
            request = new CreatePostRequest(
                    request.title(),
                    request.content(),
                    request.category(),
                    "article",
                    request.image()
            );
        }

        PostDTO created = postService.create(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id,
                                              @RequestBody UpdatePostRequest request,
                                              Authentication authentication) {
        System.out.println("AdminPostController.updatePost called with id: " + id + " by user: " + authentication.getName());
        String username = authentication.getName();
        PostDTO updated = postService.update(id, request, username);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           Authentication authentication) {
        System.out.println("AdminPostController.deletePost called with id: " + id + " by user: " + authentication.getName());
        String username = authentication.getName();
        postService.deletePost(id, username);
        return ResponseEntity.noContent().build();
    }

    // Endpoint để khóa/mở khóa bài viết (nếu cần, nhưng trong frontend admin đã bỏ nút khóa)
    @PatchMapping("/{id}/lock")
    public ResponseEntity<PostDTO> toggleLock(@PathVariable Long id, Authentication authentication) {
        System.out.println("AdminPostController.toggleLock called with id: " + id + " by user: " + authentication.getName());
        String username = authentication.getName();
        PostDTO post = postService.toggleLock(id, username);
        return ResponseEntity.ok(post);
    }
}