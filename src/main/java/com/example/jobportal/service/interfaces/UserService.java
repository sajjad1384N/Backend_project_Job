package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.AvatarResource;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getMyProfile(String userEmail);

    UserProfileResponse updateProfileAvatar(String userEmail, MultipartFile file);

    AvatarResource getMyAvatarResource(String userEmail);

    PageResponse<UserProfileResponse> getUsers(String role, String keyword, int page, int size);
}
