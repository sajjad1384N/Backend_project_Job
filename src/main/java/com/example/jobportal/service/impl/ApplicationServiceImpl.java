package com.example.jobportal.service.impl;

import com.example.jobportal.dto.ApplicationStatusUpdateRequest;
import com.example.jobportal.dto.JobApplicationResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.entity.JobApplication;
import com.example.jobportal.exception.ResourceAlreadyExistsException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.mapper.JobApplicationMapper;
import com.example.jobportal.repository.JobApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.service.ApplicationStatusMailer;
import com.example.jobportal.service.ResumeStorageService;
import com.example.jobportal.service.interfaces.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationMapper mapper;
    private final ApplicationStatusMailer applicationStatusMailer;
    private final ResumeStorageService resumeStorageService;

    @Override
    public JobApplicationResponse apply(Long jobId, String coverLetter, MultipartFile resume, String userEmail) {
        if (coverLetter == null || coverLetter.isBlank()) {
            throw new IllegalArgumentException("Cover letter is required");
        }

        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        var candidate = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (jobApplicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ResourceAlreadyExistsException("You have already applied for this job");
        }

        ResumeStorageService.StoredResume stored;
        try {
            stored = resumeStorageService.store(resume);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store resume", e);
        }

        JobApplication saved = jobApplicationRepository.save(JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(coverLetter.trim())
                .resumeStorageKey(stored.storageKey())
                .resumeOriginalFilename(stored.originalFilename())
                .build());

        return mapper.toResponse(saved);
    }

    @Override
    public ResolvedResume resolveResumeForJob(Long jobId, Long applicationId) {
        var application = jobApplicationRepository.findByIdAndJobId(applicationId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found for this job"));
        String key = application.getResumeStorageKey();
        if (key == null || key.isBlank()) {
            throw new ResourceNotFoundException("No resume on file for this application");
        }
        var path = resumeStorageService.resolveToPath(key);
        if (path == null || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Resume file missing");
        }
        String name = application.getResumeOriginalFilename();
        if (name == null || name.isBlank()) {
            name = "resume";
        }
        return new ResolvedResume(path, name);
    }

    @Override
    public PageResponse<JobApplicationResponse> getMyApplications(String userEmail, String status, String keyword, int page, int size) {
        var candidate = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        var statusFilter = parseStatus(status);

        var applicationsPage = jobApplicationRepository.searchCandidateApplications(
                        candidate.getId(),
                        statusFilter,
                        keyword,
                        pageable)
                .map(mapper::toResponse);

        return PageResponse.from(applicationsPage);
    }

    @Override
    public PageResponse<JobApplicationResponse> getByJob(Long jobId, String status, String keyword, int page, int size) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found");
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        var statusFilter = parseStatus(status);

        var applicationsPage = jobApplicationRepository.searchJobApplications(
                        jobId,
                        statusFilter,
                        keyword,
                        pageable)
                .map(mapper::toResponse);

        return PageResponse.from(applicationsPage);
    }

    @Override
    @Transactional
    public JobApplicationResponse updateStatus(Long jobId, Long applicationId, ApplicationStatusUpdateRequest request) {
        if (request.getStatus() == ApplicationStatus.APPLIED) {
            throw new IllegalArgumentException("Status can only be SHORTLISTED or REJECTED");
        }

        var application = jobApplicationRepository.findByIdAndJobId(applicationId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found for this job"));

        ApplicationStatus previous = application.getStatus();
        application.setStatus(request.getStatus());
        var saved = jobApplicationRepository.save(application);

        if (previous != request.getStatus()) {
            try {
                var candidate = saved.getCandidate();
                applicationStatusMailer.sendStatusUpdate(
                        candidate.getEmail(),
                        candidate.getFullName(),
                        saved.getJob().getTitle(),
                        request.getStatus());
            } catch (Exception e) {
                log.warn("Could not notify candidate about status change: {}", e.getMessage());
            }
        }

        return mapper.toResponse(saved);
    }

    private ApplicationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Use APPLIED, SHORTLISTED, or REJECTED");
        }
    }
}
