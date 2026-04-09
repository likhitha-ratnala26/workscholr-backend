package com.workscholr.backend.config;

import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.JobOpportunityRepository;
import com.workscholr.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    @ConditionalOnProperty(name = "app.bootstrap.admin.enabled", havingValue = "true")
    CommandLineRunner bootstrapAdmin(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     org.springframework.core.env.Environment environment) {
        return args -> ensureAdminAccount(
                userRepository,
                passwordEncoder,
                environment.getProperty("app.bootstrap.admin.full-name", "System Admin"),
                environment.getProperty("app.bootstrap.admin.email"),
                environment.getProperty("app.bootstrap.admin.password")
        );
    }

    @Bean
    @ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
    CommandLineRunner seedData(UserRepository userRepository,
                               JobOpportunityRepository jobOpportunityRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByEmail("admin@workscholr.com")
                    .orElseGet(() -> {
                        User user = new User();
                        user.setFullName("System Admin");
                        user.setEmail("admin@workscholr.com");
                        user.setPassword(passwordEncoder.encode("Admin@123"));
                        user.setRole(Role.ADMIN);
                        user.setActive(true);
                        return userRepository.save(user);
                    });

            userRepository.findByEmail("student@workscholr.com")
                    .orElseGet(() -> {
                        User user = new User();
                        user.setFullName("Demo Student");
                        user.setEmail("student@workscholr.com");
                        user.setPassword(passwordEncoder.encode("Student@123"));
                        user.setRole(Role.STUDENT);
                        user.setActive(true);
                        return userRepository.save(user);
                    });

            if (jobOpportunityRepository.count() == 0) {
                jobOpportunityRepository.save(createJob(
                        "Frontend Developer Intern",
                        "IT",
                        "Work on React-based web applications and improve user interfaces.",
                        20,
                        new BigDecimal("12000"),
                        "Main Campus",
                        admin
                ));
                jobOpportunityRepository.save(createJob(
                        "Library Assistant",
                        "Library",
                        "Support book issue tracking and student assistance at the library desk.",
                        15,
                        new BigDecimal("8000"),
                        "Central Library",
                        admin
                ));
            }
        };
    }

    User ensureAdminAccount(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            String fullName,
                            String email,
                            String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("Bootstrap admin is enabled but email or password is missing");
        }

        String normalizedEmail = email.trim();
        return userRepository.findByEmail(normalizedEmail)
                .map(existingUser -> {
                    if (existingUser.getRole() != Role.ADMIN) {
                        throw new IllegalStateException("Bootstrap admin email already belongs to a non-admin user");
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setFullName(fullName == null || fullName.isBlank() ? "System Admin" : fullName.trim());
                    user.setEmail(normalizedEmail);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setRole(Role.ADMIN);
                    user.setActive(true);
                    return userRepository.save(user);
                });
    }

    private JobOpportunity createJob(String title,
                                     String department,
                                     String description,
                                     Integer hoursPerWeek,
                                     BigDecimal stipend,
                                     String location,
                                     User admin) {
        JobOpportunity job = new JobOpportunity();
        job.setTitle(title);
        job.setDepartment(department);
        job.setDescription(description);
        job.setHoursPerWeek(hoursPerWeek);
        job.setMonthlyStipend(stipend);
        job.setLocation(location);
        job.setActive(true);
        job.setPostedBy(admin);
        return job;
    }
}
