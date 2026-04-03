package com.example.jobportal.service;

import com.example.jobportal.entity.ApplicationStatus;

public interface ApplicationStatusMailer {

    void sendStatusUpdate(String toEmail, String candidateName, String jobTitle, ApplicationStatus newStatus);
}
