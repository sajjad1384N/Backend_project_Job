package com.example.jobportal.dto;

import com.example.jobportal.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
}
