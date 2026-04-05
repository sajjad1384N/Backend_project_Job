package com.example.jobportal.service;

import com.example.jobportal.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationStatusMailer {

    void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus);

    void sendNewApplicationNotification(List<String> recruiterEmails, String jobTitle, String companyName,
                                        String candidateName, String candidateEmail);

    void sendCandidateApplicationConfirmation(String candidateEmail, String candidateName, String jobTitle);

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
