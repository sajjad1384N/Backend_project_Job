package com.example.jobportal.controller;

import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.dto.ApplicationStatusUpdateRequest;
import com.example.jobportal.dto.JobApplicationResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.service.interfaces.ApplicationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping(value = "/jobs/{jobId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<JobApplicationResponse> apply(@PathVariable Long jobId,
                                                     @RequestParam String coverLetter,
                                                     @RequestParam("resume") MultipartFile resume,
                                                     Authentication authentication) {
        return ApiResponse.ok("Applied successfully",
                applicationService.apply(jobId, coverLetter, resume, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @GetMapping("/jobs/{jobId}/{applicationId}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long jobId,
                                                   @PathVariable Long applicationId) {
        ApplicationService.ResolvedResume resolved = applicationService.resolveResumeForJob(jobId, applicationId);
        Path path = resolved.file();
        Resource body = new FileSystemResource(path.toFile());
        String ext = extension(resolved.downloadFilename());
        MediaType mediaType = switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "doc" -> MediaType.parseMediaType("application/msword");
            case "docx" -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(resolved.downloadFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(body);
    }

    private static String extension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return "";
        }
        return filename.substring(i + 1).toLowerCase();
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @GetMapping(value = "/jobs/{jobId}/export", produces = "text/csv;charset=UTF-8")
    public void exportCsv(@PathVariable Long jobId, HttpServletResponse response) throws IOException {
        String filename = "applications-" + jobId + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString());
        applicationService.writeJobApplicationsCsv(jobId, response.getWriter());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/me")
    public ApiResponse<PageResponse<JobApplicationResponse>> myApplications(Authentication authentication,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(required = false) String status,
                                                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("Applications fetched successfully",
                applicationService.getMyApplications(authentication.getName(), status, keyword, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @GetMapping("/jobs/{jobId}")
    public ApiResponse<PageResponse<JobApplicationResponse>> byJob(@PathVariable Long jobId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(required = false) String status,
                                                                   @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("Applications fetched successfully",
                applicationService.getByJob(jobId, status, keyword, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @PatchMapping("/jobs/{jobId}/{applicationId}/status")
    public ApiResponse<JobApplicationResponse> updateStatus(@PathVariable Long jobId,
                                                            @PathVariable Long applicationId,
                                                            @RequestBody @Valid ApplicationStatusUpdateRequest request) {
        return ApiResponse.ok("Application status updated",
                applicationService.updateStatus(jobId, applicationId, request));
    }
}
