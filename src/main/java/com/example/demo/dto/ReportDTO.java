package com.example.demo.dto;

import com.example.demo.entity.Report;
import java.time.LocalDateTime;

public class ReportDTO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String reportedBy;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String adminNote;

    public ReportDTO() {}

    public ReportDTO(Report report) {
        this.id = report.getId();
        this.postId = report.getPost().getId();
        this.postTitle = report.getPost().getTitle();
        this.reportedBy = report.getReportedBy();
        this.reason = report.getReason();
        this.description = report.getDescription();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        this.resolvedAt = report.getResolvedAt();
        this.resolvedBy = report.getResolvedBy();
        this.adminNote = report.getAdminNote();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}