package com.example.demo.profile;

import com.example.demo.support.PostgresTestContainer;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PostgresTestContainer.class)
class ProfileRepositoryTest {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    ProfileRepositoryTest(@Autowired ProfileRepository profileRepository, @Autowired UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Test
    void savesAndFindsProfileByUserId() {
        User alice = userRepository.save(new User("alice@example.com", "hash"));
        profileRepository.save(new Profile(alice, "Alice", "NF-2026", List.of("vue", "spring")));

        Optional<Profile> found = profileRepository.findByUser_Id(alice.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getCohort()).isEqualTo("NF-2026");
        assertThat(found.get().getFocusAreas()).containsExactly("vue", "spring");
    }

    @Test
    void returnsEmptyForUserWithoutProfile() {
        User bob = userRepository.save(new User("bob@example.com", "hash"));

        assertThat(profileRepository.findByUser_Id(bob.getId())).isEmpty();
    }

    @Test
    void rejectsSecondProfileForSameUser() {
        User carol = userRepository.save(new User("carol@example.com", "hash"));
        profileRepository.saveAndFlush(new Profile(carol, "Carol", "NF-2026", List.of()));

        assertThatThrownBy(() ->
                profileRepository.saveAndFlush(new Profile(carol, "Carol Again", "NF-2027", List.of())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
