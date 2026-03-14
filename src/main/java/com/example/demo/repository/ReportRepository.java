package com.example.demo.repository;

import com.example.demo.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Long countByStatus(String status);
}