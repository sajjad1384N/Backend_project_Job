package com.example.jobportal.controller;

import com.example.jobportal.dto.AdminDashboardStatsResponse;
import com.example.jobportal.dto.AdminJobSummaryResponse;
import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ApiResponse<AdminDashboardStatsResponse> stats() {
        return ApiResponse.ok("Dashboard stats fetched successfully", adminService.getDashboardStats());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/jobs/application-counts")
    public ApiResponse<List<AdminJobSummaryResponse>> jobsWithApplicationCounts() {
        return ApiResponse.ok("Jobs fetched successfully", adminService.getJobsWithApplicationCounts());
    }
}
