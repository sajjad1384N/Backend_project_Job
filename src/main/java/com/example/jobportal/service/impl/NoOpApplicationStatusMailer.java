package com.example.jobportal.service.impl;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.service.ApplicationStatusMailer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpApplicationStatusMailer implements ApplicationStatusMailer {

    @Override
    public void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus) {
        log.debug("Skipping application status email (app.mail.enabled=false): {} -> {} for {}", newStatus, jobTitle, toEmail);
    }

    @Override
    public void sendNewApplicationNotification(List<String> recruiterEmails, String jobTitle, String companyName,
                                               String candidateName, String candidateEmail) {
        log.debug("Skipping new-application email (app.mail.enabled=false): {} for {}", jobTitle, recruiterEmails);
    }

    @Override
    public void sendCandidateApplicationConfirmation(String candidateEmail, String candidateName, String jobTitle) {
        log.debug("Skipping candidate confirmation email (app.mail.enabled=false): {} for {}", jobTitle, candidateEmail);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.debug("Skipping password reset email (app.mail.enabled=false): {}", toEmail);
    }
}
