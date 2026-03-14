package com.example.demo.service;

import com.example.demo.dto.ReportDTO;
import com.example.demo.dto.request.ApproveReportRequest;
import com.example.demo.dto.request.RejectReportRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.Report;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    // Lấy danh sách báo cáo (phân trang)
    public List<ReportDTO> getReports(int page, int limit, String status) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Report> reportPage;
        if (status != null && !status.isEmpty()) {
            reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            reportPage = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return reportPage.stream().map(ReportDTO::new).collect(Collectors.toList());
    }

    // Lấy chi tiết một báo cáo
    public ReportDTO getReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return new ReportDTO(report);
    }

    // Phê duyệt báo cáo (có thể cập nhật bài viết)
    @Transactional
    public ReportDTO approveReport(Long id, ApproveReportRequest request, String adminUsername) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("Report is not pending");
        }

        // Nếu có cập nhật nội dung bài viết
        if (request.getUpdatedPostContent() != null && !request.getUpdatedPostContent().isEmpty()) {
            Post post = report.getPost();
            post.setContent(request.getUpdatedPostContent());
            postRepository.save(post);
        }

        report.setStatus("APPROVED");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername);
        report.setAdminNote(request.getAdminNote());
        reportRepository.save(report);

        return new ReportDTO(report);
    }

    // Từ chối báo cáo
    @Transactional
    public ReportDTO rejectReport(Long id, RejectReportRequest request, String adminUsername) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("Report is not pending");
        }

        report.setStatus("REJECTED");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername);
        report.setAdminNote(request.getAdminNote());
        reportRepository.save(report);

        return new ReportDTO(report);
    }

    // Đếm số lượng báo cáo theo trạng thái (cho dashboard)
    public long countPending() {
        return reportRepository.countByStatus("PENDING");
    }
}