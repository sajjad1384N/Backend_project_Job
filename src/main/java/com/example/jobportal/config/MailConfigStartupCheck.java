package com.example.jobportal.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Logs a clear warning at startup when Gmail SMTP is still using placeholder values.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "jobportal.mail.enabled", havingValue = "true")
public class MailConfigStartupCheck {

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @PostConstruct
    void warnIfMailNotConfigured() {
        boolean badUser = mailUsername == null || mailUsername.isBlank()
                || mailUsername.contains("your@gmail.com");
        boolean badPass = mailPassword == null || mailPassword.isBlank()
                || mailPassword.contains("REPLACE_WITH_GMAIL_APP_PASSWORD");
        if (badUser || badPass) {
            log.warn("========== EMAIL (SMTP) NOT FULLY CONFIGURED ==========");
            if (badUser) {
                log.warn("Set spring.mail.username or env MAIL_USERNAME to your real Gmail address.");
            }
            if (badPass) {
                log.warn("Set spring.mail.password or env MAIL_PASSWORD to a 16-character Gmail App Password.");
                log.warn("Google Account → Security → 2-Step Verification → App passwords (not your normal Gmail password).");
            }
            log.warn("Until this is fixed, password reset will fail with 'Authentication failed' on Gmail.");
            log.warn("========================================================");
        }
    }
}
