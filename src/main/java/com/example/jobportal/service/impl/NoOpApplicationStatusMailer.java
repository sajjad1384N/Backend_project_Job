package com.example.jobportal.service.impl;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.service.ApplicationStatusMailer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpApplicationStatusMailer implements ApplicationStatusMailer {

    @Override
    public void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus) {
        log.debug("Skipping application status email (app.mail.enabled=false): {} -> {} for {}", newStatus, jobTitle, toEmail);
    }
}
