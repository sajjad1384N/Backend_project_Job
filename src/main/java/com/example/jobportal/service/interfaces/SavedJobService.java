package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;

public interface SavedJobService {

    PageResponse<JobResponse> list(String userEmail, int page, int size);

    boolean isSaved(String userEmail, Long jobId);

    void save(String userEmail, Long jobId);

    void remove(String userEmail, Long jobId);
}
