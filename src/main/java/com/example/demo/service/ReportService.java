package com.example.demo.service;

import com.example.demo.dto.ReportDTO;
import com.example.demo.dto.request.CreateReportRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public ReportDTO createReport(Long userId, CreateReportRequest request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Report report = new Report(user, post, request.getReason(), request.getDescription());
        report = reportRepository.save(report);

        return convertToDTO(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportDTO> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReportDTO> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ReportDTO getReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
        return convertToDTO(report);
    }

    @Transactional
    public ReportDTO updateReportStatus(Long reportId, ReportStatus status, String adminNotes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

        report.setStatus(status);
        report.setAdminNotes(adminNotes);
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        return convertToDTO(report);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUser().getId());
        dto.setUsername(report.getUser().getUsername());
        dto.setPostId(report.getPost().getId());
        dto.setPostTitle(report.getPost().getTitle());
        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        dto.setAdminNotes(report.getAdminNotes());
        return dto;
    }
}