package com.example.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminJobSummaryResponse {
    private Long jobId;
    private String title;
    private String companyName;
    private Long applicationCount;
}
