package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;

public interface JobService {
    JobResponse create(JobRequest request);
    JobResponse getById(Long id);
    JobResponse update(Long id, JobRequest request);
    void delete(Long id);
    PageResponse<JobResponse> getAll(String keyword, String location, String company, int page, int size);
}
