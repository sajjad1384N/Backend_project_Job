package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Stored filename key under {@code app.profile.upload-dir}; null if no photo. */
    private String profileImageKey;

    /** BCrypt hash of the one-time password reset token; cleared after use. */
    private String passwordResetTokenHash;

    private Instant passwordResetExpiresAt;
}
