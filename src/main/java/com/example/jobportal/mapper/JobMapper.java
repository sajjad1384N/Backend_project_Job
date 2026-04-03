package com.example.jobportal.mapper;

import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(JobRequest request) {
        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .companyName(request.getCompanyName())
                .build();
    }

    public JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .companyName(job.getCompanyName())
                .build();
    }
}
