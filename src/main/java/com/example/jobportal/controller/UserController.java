package com.example.jobportal.controller;

import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;
import com.example.jobportal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> myProfile(Authentication authentication) {
        return ApiResponse.ok("Profile fetched successfully", userService.getMyProfile(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    @GetMapping
    public ApiResponse<PageResponse<UserProfileResponse>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(required = false) String role,
                                                                   @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("Users fetched successfully", userService.getUsers(role, keyword, page, size));
    }
}
