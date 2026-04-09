package com.workscholr.backend.config;

import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    private final DataInitializer dataInitializer = new DataInitializer();

    @Test
    void ensureAdminAccountCreatesAdminWhenMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Admin@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User admin = dataInitializer.ensureAdminAccount(
                userRepository,
                passwordEncoder,
                "System Admin",
                "admin@example.com",
                "Admin@123"
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals("admin@example.com", savedUser.getEmail());
        assertEquals("System Admin", savedUser.getFullName());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    void ensureAdminAccountReturnsExistingAdminWithoutChangingIt() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User existingAdmin = new User();
        existingAdmin.setId(2L);
        existingAdmin.setEmail("admin@example.com");
        existingAdmin.setFullName("Existing Admin");
        existingAdmin.setPassword("already-encoded");
        existingAdmin.setRole(Role.ADMIN);
        existingAdmin.setActive(true);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingAdmin));

        User result = dataInitializer.ensureAdminAccount(
                userRepository,
                passwordEncoder,
                "System Admin",
                "admin@example.com",
                "Admin@123"
        );

        assertSame(existingAdmin, result);
    }

    @Test
    void ensureAdminAccountRejectsEscalatingExistingStudent() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User existingStudent = new User();
        existingStudent.setEmail("student@example.com");
        existingStudent.setRole(Role.STUDENT);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(existingStudent));

        assertThrows(
                IllegalStateException.class,
                () -> dataInitializer.ensureAdminAccount(
                        userRepository,
                        passwordEncoder,
                        "System Admin",
                        "student@example.com",
                        "Admin@123"
                )
        );
    }
}
