package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(length = 2000)
    private String coverLetter;

    private String resumeStorageKey;

    private String resumeOriginalFilename;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt;

    @PrePersist
    public void onCreate() {
        if (appliedAt == null) {
            appliedAt = Instant.now();
        }
        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }
    }
}
