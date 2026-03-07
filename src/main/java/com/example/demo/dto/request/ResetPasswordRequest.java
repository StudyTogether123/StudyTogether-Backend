package com.example.demo.dto.request;

public class ResetPasswordRequest {
    private String email;
    private String newPassword;
    private String token; // token trả về sau khi verify OTP (có thể là otp hoặc một token riêng)
    // getter setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}