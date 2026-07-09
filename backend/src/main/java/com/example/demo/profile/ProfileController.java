package com.example.demo.profile;

import com.example.demo.profile.dto.ProfileResponse;
import com.example.demo.profile.dto.ProfileUpdateRequest;
import com.example.demo.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // The user id only ever comes from the verified token's principal —
    // never from a path variable, query param, or request body.
    @GetMapping
    public ProfileResponse getOwnProfile(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return profileService.getProfileForUser(currentUser.userId());
    }

    @PutMapping
    public ProfileResponse upsertOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.upsertProfileForUser(currentUser.userId(), request);
    }
}
