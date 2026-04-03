package com.example.jobportal.service.impl;

import com.example.jobportal.dto.AdminDashboardStatsResponse;
import com.example.jobportal.dto.AdminJobSummaryResponse;
import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.repository.JobApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Override
    public AdminDashboardStatsResponse getDashboardStats() {
        return new AdminDashboardStatsResponse(
                userRepository.count(),
                jobRepository.count(),
                jobApplicationRepository.count(),
                jobApplicationRepository.countByStatus(ApplicationStatus.APPLIED),
                jobApplicationRepository.countByStatus(ApplicationStatus.SHORTLISTED),
                jobApplicationRepository.countByStatus(ApplicationStatus.REJECTED)
        );
    }

    @Override
    public List<AdminJobSummaryResponse> getJobsWithApplicationCounts() {
        return jobRepository.findAllWithApplicationCounts();
    }
}
