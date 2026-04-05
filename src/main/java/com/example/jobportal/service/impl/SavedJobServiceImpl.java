package com.example.jobportal.service.impl;

import com.example.jobportal.dto.JobResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.entity.SavedJob;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.mapper.JobMapper;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SavedJobRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.service.interfaces.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    public PageResponse<JobResponse> list(String userEmail, int page, int size) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var pageable = PageRequest.of(page, size);
        var result = savedJobRepository.findByUserOrderBySavedAtDesc(user, pageable)
                .map(sj -> jobMapper.toResponse(sj.getJob()));
        return PageResponse.from(result);
    }

    @Override
    public boolean isSaved(String userEmail, Long jobId) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return savedJobRepository.existsByUserAndJobId(user, jobId);
    }

    @Override
    @Transactional
    public void save(String userEmail, Long jobId) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (savedJobRepository.existsByUserAndJobId(user, jobId)) {
            return;
        }
        savedJobRepository.save(SavedJob.builder()
                .user(user)
                .job(job)
                .savedAt(Instant.now())
                .build());
    }

    @Override
    @Transactional
    public void remove(String userEmail, Long jobId) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        savedJobRepository.deleteByUserAndJobId(user, jobId);
    }
}
