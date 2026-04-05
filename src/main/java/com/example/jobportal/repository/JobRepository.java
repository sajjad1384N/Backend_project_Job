package com.example.jobportal.repository;

import com.example.jobportal.dto.AdminJobSummaryResponse;
import com.example.jobportal.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
            SELECT new com.example.jobportal.dto.AdminJobSummaryResponse(
                j.id, j.title, j.companyName, COUNT(a.id)
            )
            FROM Job j
            LEFT JOIN JobApplication a ON a.job = j
            GROUP BY j.id, j.title, j.companyName
            ORDER BY COUNT(a.id) DESC
            """)
    List<AdminJobSummaryResponse> findAllWithApplicationCounts();
    @Query("""
            SELECT j FROM Job j
            WHERE (:keyword IS NULL OR
                   LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:company IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :company, '%')))
              AND (j.closingAt IS NULL OR j.closingAt > :now)
            """)
    Page<Job> search(@Param("keyword") String keyword,
                     @Param("location") String location,
                     @Param("company") String company,
                     @Param("now") Instant now,
                     Pageable pageable);
}
