package com.example.demo.repository;

import com.example.demo.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndOtpAndExpiryDateAfterAndUsedFalse(String email, String otp, LocalDateTime now);
    Optional<PasswordResetToken> findByEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime now); // Xóa token hết hạn
}