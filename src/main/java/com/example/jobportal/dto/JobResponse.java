package com.example.jobportal.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String companyName;
    private Instant closingAt;
    /** True when {@link #closingAt} is set and not after "now". */
    private boolean closed;
}
