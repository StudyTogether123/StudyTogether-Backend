package com.example.demo.controller.admin;

import com.example.demo.dto.ReportDTO;
import com.example.demo.entity.ReportStatus;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<Page<ReportDTO>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReportStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ReportDTO> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status, pageable);
        } else {
            reports = reportService.getReports(pageable);
        }

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReport(@PathVariable Long id) {
        ReportDTO report = reportService.getReport(id);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReport(@PathVariable Long id, @RequestParam(required = false) String notes) {
        try {
            ReportDTO updated = reportService.updateReportStatus(id, ReportStatus.APPROVED, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable Long id, @RequestParam(required = false) String notes) {
        try {
            ReportDTO updated = reportService.updateReportStatus(id, ReportStatus.REJECTED, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/count-pending")
    public ResponseEntity<Map<String, Long>> countPending() {
        long count = reportService.countPending();
        return ResponseEntity.ok(Map.of("pending", count));
    }
}