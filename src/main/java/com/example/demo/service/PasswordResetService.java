package com.example.demo.service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.Users;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;  // Service gửi email qua SendGrid API

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 10; // Thời gian hiệu lực của OTP (phút)

    /**
     * Tạo OTP ngẫu nhiên 6 chữ số
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Tạo OTP, lưu vào database và gửi qua email
     */
    @Transactional
    public void createAndSendOtp(String email) throws Exception {
        // Kiểm tra email có tồn tại không
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Xóa token cũ (nếu có) để tránh rác
        tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);

        // Tạo OTP mới
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        PasswordResetToken token = new PasswordResetToken(email, otp, expiry);
        tokenRepository.save(token);

        // Gửi email qua SendGrid API
        String subject = "Mã xác nhận đặt lại mật khẩu - StudyTogether";
        String body = "Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút.";

        boolean sent = emailService.sendEmail(email, subject, body);
        if (!sent) {
            throw new RuntimeException("Gửi email thất bại. Vui lòng thử lại sau.");
        }
    }

    /**
     * Xác thực OTP
     * @return "verified" nếu thành công
     */
    @Transactional
    public String verifyOtp(String email, String otp) {
        PasswordResetToken token = tokenRepository
                .findByEmailAndOtpAndExpiryDateAfterAndUsedFalse(email, otp, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Đánh dấu token đã được sử dụng (để tránh dùng lại OTP nhiều lần)
        token.setUsed(true);
        tokenRepository.save(token);

        // Có thể trả về thông tin xác thực (ở đây đơn giản là "verified")
        return "verified";
    }

    /**
     * Đặt lại mật khẩu mới (sau khi OTP đã được xác thực)
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tìm token đã được xác thực (used = true) và còn hạn
        PasswordResetToken token = tokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đặt lại mật khẩu"));

        // Kiểm tra token đã được xác thực và chưa hết hạn
        if (token.isUsed() && token.getExpiryDate().isAfter(LocalDateTime.now())) {
            // Mã hóa mật khẩu mới và lưu
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);

            // Xóa token sau khi đã dùng (tránh dùng lại)
            tokenRepository.delete(token);
        } else {
            throw new RuntimeException("Yêu cầu không hợp lệ hoặc đã hết hạn");
        }
    }
}