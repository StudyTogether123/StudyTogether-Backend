package com.example.demo.service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.Users;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int OTP_EXPIRY_MINUTES = 10;

    // Tạo OTP ngẫu nhiên 6 chữ số
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Gửi email OTP
    @Transactional
    public void createAndSendOtp(String email) throws Exception {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Xóa token cũ nếu có
        tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);

        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        PasswordResetToken token = new PasswordResetToken(email, otp, expiry);
        tokenRepository.save(token);

        // Gửi email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác nhận đặt lại mật khẩu - StudyTogether");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút.");
        mailSender.send(message);
    }

    // Xác thực OTP
    @Transactional
    public String verifyOtp(String email, String otp) {
        PasswordResetToken token = tokenRepository
                .findByEmailAndOtpAndExpiryDateAfterAndUsedFalse(email, otp, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Đánh dấu token là đã dùng? Có thể đánh dấu used = true và tạo token tạm cho reset password, hoặc để nguyên và xóa sau khi reset.
        // Ở đây ta chỉ cần xác thực thành công, không đánh dấu used ngay, để dùng cho reset. Nhưng nên có cơ chế tránh dùng lại nhiều lần.
        // Tôi sẽ trả về một mã xác nhận tạm thời (ví dụ token id) để dùng ở bước reset, đồng thời đánh dấu used.
        token.setUsed(true);
        tokenRepository.save(token);

        // Có thể tạo một token ngắn hạn (JWT?) hoặc đơn giản trả về "success"
        return "verified"; // hoặc có thể trả về token xác thực
    }

    // Đặt lại mật khẩu
    @Transactional
    public void resetPassword(String email, String newPassword) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra xem đã có OTP verified chưa? Có thể kiểm tra token used gần nhất.
        // Ở đây ta giả định rằng trước khi gọi reset, OTP đã được xác thực thành công (used = true) và chưa quá hạn.
        // Ta có thể xóa token sau khi reset hoặc giữ lại để tránh dùng lại.
        // Tìm token verified gần nhất của email
        PasswordResetToken token = tokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đặt lại mật khẩu"));

        if (token.isUsed() && token.getExpiryDate().isAfter(LocalDateTime.now())) {
            // Hợp lệ, tiến hành đổi mật khẩu
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);
            // Xóa token sau khi đã dùng
            tokenRepository.delete(token);
        } else {
            throw new RuntimeException("Yêu cầu không hợp lệ hoặc đã hết hạn");
        }
    }
}