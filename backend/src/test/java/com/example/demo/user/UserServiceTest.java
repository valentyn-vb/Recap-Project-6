package com.example.demo.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registerUserStoresHashedPasswordNotRawPassword() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserService userService = new UserService(userRepository, passwordEncoder);
        User saved = userService.registerUser("alice@example.com", "s3cret-password");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertThat(persisted.getEmail()).isEqualTo("alice@example.com");
        assertThat(persisted.getPasswordHash()).isNotEqualTo("s3cret-password");
        assertThat(passwordEncoder.matches("s3cret-password", persisted.getPasswordHash())).isTrue();
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void registerUserRejectsDuplicateEmailWithoutSaving() {
        when(userRepository.findByEmail("taken@example.com"))
                .thenReturn(Optional.of(new User("taken@example.com", "hash")));

        UserService userService = new UserService(userRepository, passwordEncoder);

        assertThatThrownBy(() -> userService.registerUser("taken@example.com", "irrelevant"))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(userRepository, never()).save(any());
    }
}
