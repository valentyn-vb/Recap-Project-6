package com.example.demo.user;

import com.example.demo.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PostgresTestContainer.class)
class UserRepositoryTest {

    private final UserRepository userRepository;

    UserRepositoryTest(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void savesAndRetrievesUserById() {
        User saved = userRepository.save(new User("alice@example.com", "hashed-password"));

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findsUserByEmail() {
        userRepository.save(new User("bob@example.com", "hash"));

        assertThat(userRepository.findByEmail("bob@example.com")).isPresent();
        assertThat(userRepository.findByEmail("unknown@example.com")).isEmpty();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(new User("carol@example.com", "hash1"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User("carol@example.com", "hash2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
