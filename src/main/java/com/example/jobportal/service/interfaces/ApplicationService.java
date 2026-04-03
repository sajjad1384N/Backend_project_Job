package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.ApplicationStatusUpdateRequest;
import com.example.jobportal.dto.JobApplicationResponse;
import com.example.jobportal.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ApplicationService {

    record ResolvedResume(Path file, String downloadFilename) {}

    JobApplicationResponse apply(Long jobId, String coverLetter, MultipartFile resume, String userEmail);

    ResolvedResume resolveResumeForJob(Long jobId, Long applicationId);
    PageResponse<JobApplicationResponse> getMyApplications(String userEmail, String status, String keyword, int page, int size);
    PageResponse<JobApplicationResponse> getByJob(Long jobId, String status, String keyword, int page, int size);
    JobApplicationResponse updateStatus(Long jobId, Long applicationId, ApplicationStatusUpdateRequest request);
}
