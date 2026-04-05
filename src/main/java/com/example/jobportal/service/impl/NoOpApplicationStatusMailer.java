package com.example.jobportal.service.impl;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.service.ApplicationStatusMailer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "jobportal.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpApplicationStatusMailer implements ApplicationStatusMailer {

    @Value("${jobportal.mail.log-reset-link-when-disabled:true}")
    private boolean logResetLinkWhenDisabled;

    @Override
    public void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus) {
        log.debug("Skipping application status email (jobportal.mail.enabled=false): {} -> {} for {}", newStatus, jobTitle, toEmail);
    }

    @Override
    public void sendNewApplicationNotification(List<String> recruiterEmails, String jobTitle, String companyName,
                                               String candidateName, String candidateEmail) {
        log.debug("Skipping new-application email (jobportal.mail.enabled=false): {} for {}", jobTitle, recruiterEmails);
    }

    @Override
    public void sendCandidateApplicationConfirmation(String candidateEmail, String candidateName, String jobTitle) {
        log.debug("Skipping candidate confirmation email (jobportal.mail.enabled=false): {} for {}", jobTitle, candidateEmail);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (logResetLinkWhenDisabled) {
            log.info(
                    "Password reset (SMTP off — set jobportal.mail.enabled=true + spring.mail.*): open within 1h — {}",
                    resetLink);
        } else {
            log.debug("Skipping password reset email (jobportal.mail.enabled=false): {}", toEmail);
        }
    }
}
