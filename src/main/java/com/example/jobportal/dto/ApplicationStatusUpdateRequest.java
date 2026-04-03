package com.example.jobportal.dto;

import com.example.jobportal.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull
    private ApplicationStatus status;
}
