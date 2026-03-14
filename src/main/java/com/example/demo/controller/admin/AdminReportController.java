package com.example.demo.controller.admin;

import com.example.demo.dto.ReportDTO;
import com.example.demo.dto.request.ApproveReportRequest;
import com.example.demo.dto.request.RejectReportRequest;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status) {

        List<ReportDTO> reports = reportService.getReports(page, limit, status);
        Map<String, Object> response = new HashMap<>();
        response.put("reports", reports);
        response.put("page", page);
        response.put("limit", limit);
        // có thể thêm total pages nếu cần
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReport(@PathVariable Long id) {
        ReportDTO report = reportService.getReport(id);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReportDTO> approveReport(
            @PathVariable Long id,
            @RequestBody ApproveReportRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();

        ReportDTO report = reportService.approveReport(id, request, adminUsername);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ReportDTO> rejectReport(
            @PathVariable Long id,
            @RequestBody RejectReportRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();

        ReportDTO report = reportService.rejectReport(id, request, adminUsername);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", reportService.countPending());
        // có thể thêm approved, rejected nếu cần
        return ResponseEntity.ok(stats);
    }
}