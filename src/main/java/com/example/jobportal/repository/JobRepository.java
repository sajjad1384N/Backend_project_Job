package com.example.jobportal.repository;

import com.example.jobportal.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("""
            SELECT j FROM Job j
            WHERE (:keyword IS NULL OR
                   LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:company IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :company, '%')))
            """)
    Page<Job> search(@Param("keyword") String keyword,
                     @Param("location") String location,
                     @Param("company") String company,
                     Pageable pageable);
}
