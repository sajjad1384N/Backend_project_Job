package com.example.jobportal.service.impl;

import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.mapper.JobMapper;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.service.interfaces.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    public JobResponse create(JobRequest request) {
        var saved = jobRepository.save(jobMapper.toEntity(request));
        return jobMapper.toResponse(saved);
    }

    @Override
    public JobResponse getById(Long id) {
        var job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return jobMapper.toResponse(job);
    }

    @Override
    public JobResponse update(Long id, JobRequest request) {
        var existing = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setLocation(request.getLocation());
        existing.setCompanyName(request.getCompanyName());
        existing.setClosingAt(request.getClosingAt());

        return jobMapper.toResponse(jobRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found");
        }
        jobRepository.deleteById(id);
    }

    @Override
    public PageResponse<JobResponse> getAll(String keyword, String location, String company, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var jobsPage = jobRepository.search(keyword, location, company, Instant.now(), pageable)
                .map(jobMapper::toResponse);
        return PageResponse.from(jobsPage);
    }
}
