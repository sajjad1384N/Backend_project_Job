package com.example.jobportal.dto;

import com.example.jobportal.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeOriginalFilename;
    private Instant appliedAt;
}
