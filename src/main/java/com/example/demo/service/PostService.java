package com.example.demo.service;

import com.example.demo.dto.PostDTO;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.request.UpdatePostRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.Users;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UsersRepository usersRepository;

    public PostService(PostRepository postRepository, UsersRepository usersRepository) {
        this.postRepository = postRepository;
        this.usersRepository = usersRepository;
    }

    // ========== Lấy tất cả bài viết ==========
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ========== Lấy bài viết theo ID ==========
    public PostDTO getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
    }

    // ========== Tạo bài viết mới ==========
    @Transactional
    public PostDTO create(CreatePostRequest request, String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Post post = new Post(
                request.title(),
                request.content(),
                username,
                request.category()
        );

        Post saved = postRepository.save(post);
        return mapToDTO(saved);
    }

    // ========== Cập nhật bài viết (có log chi tiết) ==========
    @Transactional
    public PostDTO update(Long id, UpdatePostRequest request, String username) {
        System.out.println("=== PostService.update: Bắt đầu xử lý ===");
        System.out.println("ID bài viết: " + id);
        System.out.println("Người dùng: " + username);
        System.out.println("Request: title=" + request.title() + ", content=" + request.content() + ", category=" + request.category() + ", locked=" + request.locked());

        try {
            // 1. Tìm bài viết
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với id: " + id));
            System.out.println("Đã tìm thấy bài viết, tác giả: " + post.getAuthor());

            // 2. Kiểm tra quyền
            checkPermission(post, username);
            System.out.println("Kiểm tra quyền thành công");

            // 3. Cập nhật các trường, chỉ set nếu request có giá trị (không null)
            if (request.title() != null) {
                post.setTitle(request.title());
            }
            if (request.content() != null) {
                post.setContent(request.content());
            }
            if (request.category() != null) {
                post.setCategory(request.category());
            }
            if (request.locked() != null) {
                post.setLocked(request.locked());
            }
            // Nếu request không gửi locked, giữ nguyên giá trị cũ
            System.out.println("Đã cập nhật các trường");

            // 4. Lưu
            Post updated = postRepository.save(post);
            System.out.println("Đã lưu bài viết thành công, ID: " + updated.getId());

            // 5. Chuyển đổi DTO
            PostDTO dto = mapToDTO(updated);
            System.out.println("=== Kết thúc thành công ===");
            return dto;

        } catch (Exception e) {
            System.out.println("!!! Lỗi trong PostService.update: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ========== Xóa bài viết ==========
    @Transactional
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với id: " + id));

        checkPermission(post, username);
        postRepository.deleteById(id);
    }

    // ========== Helper: kiểm tra quyền ==========
    private void checkPermission(Post post, String username) {
        // Nếu là tác giả -> cho phép
        if (post.getAuthor().equals(username)) {
            return;
        }

        // Nếu không phải tác giả, kiểm tra xem có phải admin không
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        boolean isAdmin = user.getRole() != null && 
                (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    // ========== Chuyển đổi Entity -> DTO ==========
    private PostDTO mapToDTO(Post post) {
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getCategory(),
                post.getLocked(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getImage()
        );
    }
}