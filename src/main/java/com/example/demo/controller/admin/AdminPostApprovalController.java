package com.example.demo.controller.admin;

import com.example.demo.dto.PostDTO;
import com.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts/approval")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostApprovalController {

    private final PostService postService;

    public AdminPostApprovalController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<PostDTO>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getPendingPosts(pageable));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approvePost(@PathVariable Long id) {
        postService.approvePost(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectPost(@PathVariable Long id) {
        postService.rejectPost(id);
        return ResponseEntity.ok().build();
    }
}