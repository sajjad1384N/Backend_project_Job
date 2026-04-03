package com.example.jobportal.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record AvatarResource(Resource resource, MediaType mediaType) {}
