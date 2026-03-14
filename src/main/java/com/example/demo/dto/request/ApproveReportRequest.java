package com.example.demo.dto.request;

public class ApproveReportRequest {
    private String adminNote;
    private String updatedPostContent; // nội dung bài viết đã chỉnh sửa (nếu có)

    // getters/setters
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public String getUpdatedPostContent() { return updatedPostContent; }
    public void setUpdatedPostContent(String updatedPostContent) { this.updatedPostContent = updatedPostContent; }
}