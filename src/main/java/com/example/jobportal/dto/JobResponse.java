package com.example.jobportal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String companyName;
}
