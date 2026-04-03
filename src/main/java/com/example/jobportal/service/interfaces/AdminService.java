package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.AdminDashboardStatsResponse;
import com.example.jobportal.dto.AdminJobSummaryResponse;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsResponse getDashboardStats();

    List<AdminJobSummaryResponse> getJobsWithApplicationCounts();
}
