package com.example.jobportal.controller;

import com.example.jobportal.dto.ApiResponse;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;
import com.example.jobportal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/avatar")
    public ResponseEntity<Resource> getMyAvatar(Authentication authentication) {
        var ar = userService.getMyAvatarResource(authentication.getName());
        return ResponseEntity.ok().contentType(ar.mediaType()).body(ar.resource());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileResponse> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication) {
        return ApiResponse.ok("Profile photo updated", userService.updateProfileAvatar(authentication.getName(), file));
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
