package com.example.demo.profile;

import com.example.demo.profile.dto.ProfileResponse;
import com.example.demo.profile.dto.ProfileUpdateRequest;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void getProfileReturnsResponseForOwnUserIdOnly() {
        User alice = new User("alice@example.com", "hash");
        Profile profile = new Profile(alice, "Alice", "NF-2026", List.of("vue"));
        when(profileRepository.findByUser_Id(42L)).thenReturn(Optional.of(profile));

        ProfileService profileService = new ProfileService(profileRepository, userRepository);
        ProfileResponse response = profileService.getProfileForUser(42L);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.cohort()).isEqualTo("NF-2026");
        assertThat(response.focusAreas()).containsExactly("vue");

        // guardrail: the query must be scoped to exactly the passed-in principal id
        verify(profileRepository).findByUser_Id(eq(42L));
        verify(profileRepository, never()).findAll();
    }

    @Test
    void getProfileThrowsWhenUserHasNoProfile() {
        when(profileRepository.findByUser_Id(anyLong())).thenReturn(Optional.empty());

        ProfileService profileService = new ProfileService(profileRepository, userRepository);

        assertThatThrownBy(() -> profileService.getProfileForUser(42L))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void upsertCreatesProfileWhenNoneExists() {
        User alice = new User("alice@example.com", "hash");
        when(profileRepository.findByUser_Id(42L)).thenReturn(Optional.empty());
        when(userRepository.findById(42L)).thenReturn(Optional.of(alice));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileService profileService = new ProfileService(profileRepository, userRepository);
        ProfileResponse response = profileService.upsertProfileForUser(42L,
                new ProfileUpdateRequest("Alice", "NF-2026", List.of("vue", "spring")));

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(alice);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.focusAreas()).containsExactly("vue", "spring");
    }

    @Test
    void upsertUpdatesExistingProfileInPlace() {
        User alice = new User("alice@example.com", "hash");
        Profile existing = new Profile(alice, "Old Name", "Old Cohort", List.of("old"));
        when(profileRepository.findByUser_Id(42L)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileService profileService = new ProfileService(profileRepository, userRepository);
        ProfileResponse response = profileService.upsertProfileForUser(42L,
                new ProfileUpdateRequest("New Name", "New Cohort", List.of("new")));

        verify(profileRepository).save(existing);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCohort()).isEqualTo("New Cohort");
        assertThat(existing.getFocusAreas()).containsExactly("new");
        assertThat(response.name()).isEqualTo("New Name");
    }
}
