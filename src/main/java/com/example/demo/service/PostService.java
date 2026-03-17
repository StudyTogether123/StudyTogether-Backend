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

    // ========== Lấy tất cả bài viết (public) ==========
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ========== Lấy bài viết theo ID (public) ==========
    public PostDTO getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
    }

    // ========== Tạo bài viết mới (chỉ admin, author tự động) ==========
    @Transactional
    public PostDTO create(CreatePostRequest request, String username) {
        // Lấy thông tin user để kiểm tra quyền (có thể bỏ qua nếu chỉ dùng để lấy author)
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo bài viết với author là username
        Post post = new Post(
                request.title(),
                request.content(),
                username,  // author từ token
                request.category()
        );

        // Nếu request có trường image (chưa có trong CreatePostRequest hiện tại)
        // if (request.image() != null) post.setImage(request.image());

        Post saved = postRepository.save(post);
        return mapToDTO(saved);
    }

    // ========== Cập nhật bài viết (chỉ admin hoặc tác giả) ==========
    @Transactional
    public PostDTO update(Long id, UpdatePostRequest request, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với id: " + id));

        // Kiểm tra quyền: admin hoặc chính tác giả
        checkPermission(post, username);

        // Cập nhật các trường
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCategory(request.category());
        post.setLocked(request.locked());

        // Nếu request có trường image (chưa có trong UpdatePostRequest hiện tại)
        // if (request.image() != null) post.setImage(request.image());

        Post updated = postRepository.save(post);
        return mapToDTO(updated);
    }

    // ========== Xóa bài viết (chỉ admin hoặc tác giả) ==========
    @Transactional
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với id: " + id));

        // Kiểm tra quyền: admin hoặc chính tác giả
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