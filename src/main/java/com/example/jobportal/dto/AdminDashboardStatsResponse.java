package com.example.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    private long totalUsers;
    private long totalJobs;
    private long totalApplications;
    private long totalApplied;
    private long totalShortlisted;
    private long totalRejected;
}
