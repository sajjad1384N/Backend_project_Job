package com.example.jobportal.repository;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobId(Long jobId);
    Optional<JobApplication> findByIdAndJobId(Long id, Long jobId);
    long countByStatus(ApplicationStatus status);

    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.candidate.id = :candidateId
              AND (:status IS NULL OR a.status = :status)
              AND (:keyword IS NULL OR
                   LOWER(a.job.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.job.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<JobApplication> searchCandidateApplications(@Param("candidateId") Long candidateId,
                                                     @Param("status") ApplicationStatus status,
                                                     @Param("keyword") String keyword,
                                                     Pageable pageable);

    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.job.id = :jobId
              AND (:status IS NULL OR a.status = :status)
              AND (:keyword IS NULL OR
                   LOWER(a.candidate.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.candidate.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<JobApplication> searchJobApplications(@Param("jobId") Long jobId,
                                               @Param("status") ApplicationStatus status,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);
}
