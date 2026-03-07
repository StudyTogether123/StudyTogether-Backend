package com.example.demo.dto.request;

public class VerifyOtpRequest {
    private String email;
    private String otp;
    // getter setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}