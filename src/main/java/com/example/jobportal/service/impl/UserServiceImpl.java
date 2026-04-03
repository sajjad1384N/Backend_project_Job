package com.example.jobportal.service.impl;

import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserProfileResponse;
import com.example.jobportal.entity.Role;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getMyProfile(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    @Override
    public PageResponse<UserProfileResponse> getUsers(String role, String keyword, int page, int size) {
        var roleFilter = parseRole(role);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "fullName"));

        var usersPage = userRepository.search(roleFilter, keyword, pageable)
                .map(user -> toProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole()));

        return PageResponse.from(usersPage);
    }

    private UserProfileResponse toProfileResponse(Long id, String fullName, String email, Role role) {
        return UserProfileResponse.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .role(role)
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
