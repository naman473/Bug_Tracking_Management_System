package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository  userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService     authService;

    @BeforeEach
    void setUp() {
        userRepository  = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService     = new AuthService(userRepository, passwordEncoder);
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_unknownRole_returnsFailure() {
        RegistrationResult result = authService.register("a@b.com", "Wizard", "Pass1!");
        assertThat(result).isInstanceOf(RegistrationResult.Failure.class);
        assertThat(((RegistrationResult.Failure) result).code())
                .isEqualTo(RegistrationResult.FailureCode.UNKNOWN_ROLE);
    }

    @Test
    void register_alreadyRegistered_returnsFailure() {
        when(userRepository.findByEmail("a@b.com"))
                .thenReturn(Optional.of(new User()));
        RegistrationResult result = authService.register("a@b.com", "Developer", "Pass1!");
        assertThat(((RegistrationResult.Failure) result).code())
                .isEqualTo(RegistrationResult.FailureCode.ALREADY_REGISTERED);
    }

    @Test
    void register_notInAllowlist_returnsFailure() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findImportedByEmail(any())).thenReturn(Optional.empty());
        RegistrationResult result = authService.register("ghost@b.com", "Developer", "Pass1!");
        assertThat(((RegistrationResult.Failure) result).code())
                .isEqualTo(RegistrationResult.FailureCode.NOT_IN_ALLOWLIST);
    }

    @Test
    void register_roleMismatch_returnsFailure() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        User imported = new User();
        imported.setRole(Role.TESTER);
        imported.setUserId("u1");
        imported.setUserName("Alice");
        imported.setEmailId("a@b.com");
        when(userRepository.findImportedByEmail("a@b.com")).thenReturn(Optional.of(imported));

        RegistrationResult result = authService.register("a@b.com", "Developer", "Pass1!");
        assertThat(((RegistrationResult.Failure) result).code())
                .isEqualTo(RegistrationResult.FailureCode.ROLE_MISMATCH);
    }

    @Test
    void register_happyPath_returnsSuccess() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        User imported = new User();
        imported.setRole(Role.DEVELOPER);
        imported.setUserId("u1");
        imported.setUserName("Alice");
        imported.setEmailId("a@b.com");
        when(userRepository.findImportedByEmail("a@b.com")).thenReturn(Optional.of(imported));
        when(userRepository.insertRegisteredUser(any())).thenReturn(1);

        RegistrationResult result = authService.register("a@b.com", "Developer", "Pass1!");
        assertThat(result).isInstanceOf(RegistrationResult.Success.class);
        verify(userRepository).insertRegisteredUser(any());
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    void changePassword_wrongCurrentPassword_returnsFailure() {
        User u = new User();
        u.setPassword(passwordEncoder.encode("OldPass1!"));
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        ChangePasswordResult result = authService.changePassword("a@b.com", "WrongPass1!", "NewPass1!");
        assertThat(result).isInstanceOf(ChangePasswordResult.Failure.class);
        verify(userRepository, never()).updatePassword(any(), any());
    }

    @Test
    void changePassword_correctCurrentPassword_updatesPassword() {
        String encoded = passwordEncoder.encode("OldPass1!");
        User u = new User();
        u.setPassword(encoded);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        ChangePasswordResult result = authService.changePassword("a@b.com", "OldPass1!", "NewPass1!");
        assertThat(result).isInstanceOf(ChangePasswordResult.Success.class);
        verify(userRepository).updatePassword(eq("a@b.com"), any());
    }
}
