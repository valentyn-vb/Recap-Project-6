package com.example.demo.profile;

import com.example.demo.profile.dto.ProfileResponse;
import com.example.demo.profile.dto.ProfileUpdateRequest;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileForUser(Long userId) {
        return profileRepository.findByUser_Id(userId)
                .map(ProfileService::toResponse)
                .orElseThrow(ProfileNotFoundException::new);
    }

    @Transactional
    public ProfileResponse upsertProfileForUser(Long userId, ProfileUpdateRequest request) {
        Profile profile = profileRepository.findByUser_Id(userId)
                .map(existing -> {
                    existing.update(request.name(), request.cohort(), request.focusAreas());
                    return existing;
                })
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Authenticated user " + userId + " no longer exists"));
                    return new Profile(user, request.name(), request.cohort(), request.focusAreas());
                });
        return toResponse(profileRepository.save(profile));
    }

    private static ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(profile.getId(), profile.getName(), profile.getCohort(), profile.getFocusAreas());
    }
}
