package com.workscholr.backend.service;

import com.workscholr.backend.config.MapperConfig;
import com.workscholr.backend.dto.AuthDtos.AuthResponse;
import com.workscholr.backend.dto.AuthDtos.RegisterRequest;
import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.UserRepository;
import com.workscholr.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registerAlwaysCreatesAStudentAccount() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = new JwtService();

        when(userRepository.existsByEmail("newstudent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Student@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(101L);
            return user;
        });

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "plain-text-secret-key-with-at-least-32-characters!"
        );
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L);

        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                new MapperConfig().modelMapper()
        );

        AuthResponse response = authService.register(
                new RegisterRequest("New Student", "newstudent@example.com", "Student@123")
        );

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(savedUserCaptor.capture());

        User savedUser = savedUserCaptor.getValue();
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertEquals(Role.STUDENT, response.role());
        assertEquals("newstudent@example.com", response.email());
        assertNotNull(response.token());
    }
}
