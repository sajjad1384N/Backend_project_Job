package com.example.jobportal.repository;

import com.example.jobportal.entity.SavedJob;
import com.example.jobportal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Optional<SavedJob> findByUserAndJobId(User user, Long jobId);

    boolean existsByUserAndJobId(User user, Long jobId);

    void deleteByUserAndJobId(User user, Long jobId);

    Page<SavedJob> findByUserOrderBySavedAtDesc(User user, Pageable pageable);
}
