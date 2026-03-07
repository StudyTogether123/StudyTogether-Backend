package com.example.demo.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    /**
     * Gửi email qua SendGrid Web API
     * @param toEmail email người nhận
     * @param subject chủ đề
     * @param body nội dung (text)
     * @return true nếu gửi thành công
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        Email from = new Email("kkshi4sgg6@gmail.com"); // Thay bằng email đã xác thực trên SendGrid
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Email sent successfully: " + response.getBody());
                return true;
            } else {
                System.err.println("SendGrid error: " + response.getBody());
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}