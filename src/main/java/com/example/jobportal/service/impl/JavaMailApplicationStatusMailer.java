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
import org.springframework.util.CollectionUtils;

import jakarta.mail.AuthenticationFailedException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jobportal.mail.enabled", havingValue = "true")
public class JavaMailApplicationStatusMailer implements ApplicationStatusMailer {

    private final JavaMailSender mailSender;

    @Value("${jobportal.mail.from:noreply@jobportal.local}")
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

    @Override
    public void sendNewApplicationNotification(List<String> recruiterEmails, String jobTitle, String companyName,
                                               String candidateName, String candidateEmail) {
        if (CollectionUtils.isEmpty(recruiterEmails)) {
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(recruiterEmails.toArray(String[]::new));
        msg.setSubject("New application: " + jobTitle);
        msg.setText("A candidate has applied for \"" + jobTitle + "\" at " + companyName + ".\n\n"
                + "Candidate: " + candidateName + " <" + candidateEmail + ">\n\n"
                + "Open the portal to review applications and download resumes.\n\n"
                + "— Job Portal");
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send new-application notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendCandidateApplicationConfirmation(String candidateEmail, String candidateName, String jobTitle) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(candidateEmail);
        msg.setSubject("Application received — " + jobTitle);
        msg.setText("Dear " + candidateName + ",\n\n"
                + "We received your application for \"" + jobTitle + "\".\n\n"
                + "The hiring team will review your profile and may contact you if there is a match.\n\n"
                + "— Job Portal");
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send candidate confirmation to {}: {}", candidateEmail, e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Reset your Job Portal password");
        msg.setText("You asked to reset your password.\n\n"
                + "Open this link (valid for a limited time):\n" + resetLink + "\n\n"
                + "If you did not request this, you can ignore this email.\n\n"
                + "— Job Portal");
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            logGmailAuthHintsIfNeeded(e);
            throw new IllegalStateException("Could not send password reset email", e);
        }
    }

    /**
     * Gmail returns "Authentication failed" when the password is not a valid App Password or username/from mismatch.
     */
    private static void logGmailAuthHintsIfNeeded(Throwable e) {
        Throwable cur = e;
        for (int depth = 0; depth < 8 && cur != null; depth++) {
            if (cur instanceof AuthenticationFailedException) {
                logGmailHint();
                return;
            }
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("Authentication failed")
                    || msg.contains("535")
                    || msg.contains("534"))) {
                logGmailHint();
                return;
            }
            cur = cur.getCause();
        }
    }

    private static void logGmailHint() {
        log.warn(
                "Gmail SMTP authentication: use spring.mail.username = your full Gmail address; "
                        + "spring.mail.password = 16-character App Password (Google Account → Security → "
                        + "2-Step Verification → App passwords). Do not use your normal Gmail password. "
                        + "jobportal.mail.from must match that Gmail account. "
                        + "Or set MAIL_USERNAME and MAIL_PASSWORD env vars.");
    }
}
