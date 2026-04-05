package com.example.jobportal.controller;

import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.service.interfaces.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public ApiResponse<PageResponse<JobResponse>> list(Authentication authentication,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Saved jobs fetched successfully",
                savedJobService.list(authentication.getName(), page, size));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/jobs/{jobId}/saved")
    public ApiResponse<Boolean> isSaved(@PathVariable Long jobId, Authentication authentication) {
        return ApiResponse.ok("OK", savedJobService.isSaved(authentication.getName(), jobId));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/jobs/{jobId}")
    public ApiResponse<Void> save(@PathVariable Long jobId, Authentication authentication) {
        savedJobService.save(authentication.getName(), jobId);
        return ApiResponse.ok("Job saved", null);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/jobs/{jobId}")
    public ApiResponse<Void> remove(@PathVariable Long jobId, Authentication authentication) {
        savedJobService.remove(authentication.getName(), jobId);
        return ApiResponse.ok("Job removed from saved list", null);
    }
}
