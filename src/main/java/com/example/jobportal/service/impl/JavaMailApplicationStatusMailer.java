package com.example.jobportal.service.impl;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.service.ApplicationStatusMailer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class JavaMailApplicationStatusMailer implements ApplicationStatusMailer {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@jobportal.local}")
    private String fromAddress;

    @Override
    public void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        if (newStatus == ApplicationStatus.SHORTLISTED) {
            msg.setSubject("Congratulations — you have been shortlisted");
            msg.setText("Dear " + candidateName + ",\n\n"
                    + "Good news: your application for \"" + jobTitle + "\" has been shortlisted.\n\n"
                    + "The hiring team may contact you with next steps. Thank you for applying.\n\n"
                    + "— Job Portal");
        } else if (newStatus == ApplicationStatus.REJECTED) {
            msg.setSubject("Update on your job application");
            msg.setText("Dear " + candidateName + ",\n\n"
                    + "Thank you for your interest in \"" + jobTitle + "\". "
                    + "After careful review, we will not be moving forward with your application at this time.\n\n"
                    + "We appreciate the time you invested and wish you success in your search.\n\n"
                    + "— Job Portal");
        } else {
            return;
        }
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send application status email to {}: {}", toEmail, e.getMessage());
        }
    }
}
