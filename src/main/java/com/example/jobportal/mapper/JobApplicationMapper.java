package com.example.jobportal.mapper;

import com.example.jobportal.dto.JobApplicationResponse;
import com.example.jobportal.entity.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public JobApplicationResponse toResponse(JobApplication application) {
        return JobApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .resumeOriginalFilename(application.getResumeOriginalFilename())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}
