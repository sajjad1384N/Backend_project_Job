package com.example.jobportal.service.impl;

import com.example.jobportal.dto.AvatarResource;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.service.ProfileImageStorageService;
import com.example.jobportal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String AVATAR_API_PATH = "users/me/avatar";

    private final UserRepository userRepository;
    private final ProfileImageStorageService profileImageStorage;

    @Override
    public UserProfileResponse getMyProfile(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfileAvatar(String userEmail, MultipartFile file) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String newKey;
        try {
            newKey = profileImageStorage.store(file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store profile image", e);
        }
        String oldKey = user.getProfileImageKey();
        user.setProfileImageKey(newKey);
        userRepository.save(user);
        profileImageStorage.deleteIfExists(oldKey);
        return toProfileResponse(user);
    }

    @Override
    public AvatarResource getMyAvatarResource(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String key = user.getProfileImageKey();
        if (key == null || key.isBlank()) {
            throw new ResourceNotFoundException("No profile photo");
        }
        Path path = profileImageStorage.resolveToPath(key);
        if (path == null || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Profile photo file missing");
        }
        Resource resource = new FileSystemResource(path.toFile());
        MediaType mediaType = profileImageStorage.mediaTypeForKey(key);
        return new AvatarResource(resource, mediaType);
    }

    @Override
    public PageResponse<UserProfileResponse> getUsers(String role, String keyword, int page, int size) {
        var roleFilter = parseRole(role);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "fullName"));

        var usersPage = userRepository.search(roleFilter, keyword, pageable)
                .map(this::toProfileResponse);

        return PageResponse.from(usersPage);
    }

    private UserProfileResponse toProfileResponse(User user) {
        boolean hasAvatar = user.getProfileImageKey() != null && !user.getProfileImageKey().isBlank();
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(hasAvatar ? AVATAR_API_PATH : null)
                .build();
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Use ADMIN, RECRUITER, or CANDIDATE");
        }
    }
}
