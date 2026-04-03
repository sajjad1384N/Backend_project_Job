package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String userEmail);
    PageResponse<UserProfileResponse> getUsers(String role, String keyword, int page, int size);
}
