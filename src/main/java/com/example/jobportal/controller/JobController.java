package com.example.jobportal.controller;

import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.service.interfaces.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/{id}")
    public ApiResponse<JobResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("Job fetched successfully", jobService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @PostMapping
    public ApiResponse<JobResponse> create(@RequestBody @Valid JobRequest request) {
        return ApiResponse.ok("Job created successfully", jobService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @PutMapping("/{id}")
    public ApiResponse<JobResponse> update(@PathVariable Long id, @RequestBody @Valid JobRequest request) {
        return ApiResponse.ok("Job updated successfully", jobService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ApiResponse.ok("Job deleted successfully", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<JobResponse>> getAll(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String location,
                                                         @RequestParam(required = false) String company) {
        return ApiResponse.ok("Jobs fetched successfully",
                jobService.getAll(keyword, location, company, page, size));
    }
}
