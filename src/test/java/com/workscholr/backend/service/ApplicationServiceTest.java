package com.workscholr.backend.service;

import com.workscholr.backend.config.MapperConfig;
import com.workscholr.backend.exception.ResourceNotFoundException;
import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.StudentApplication;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.JobOpportunityRepository;
import com.workscholr.backend.repository.StudentApplicationRepository;
import com.workscholr.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEntityForCurrentStudentReturnsOwnedApplication() {
        User student = createUser(7L, "student@example.com", Role.STUDENT);
        StudentApplication application = new StudentApplication();
        application.setId(11L);

        ApplicationService applicationService = createApplicationService(student, Optional.of(application));

        StudentApplication result = applicationService.getEntityForCurrentStudent(11L);

        assertSame(application, result);
    }

    @Test
    void getEntityForCurrentStudentThrowsWhenApplicationIsNotOwnedByStudent() {
        User student = createUser(7L, "student@example.com", Role.STUDENT);
        ApplicationService applicationService = createApplicationService(student, Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.getEntityForCurrentStudent(11L));
    }

    private ApplicationService createApplicationService(User currentUser,
                                                        Optional<StudentApplication> ownedApplication) {
        setAuthenticatedUser(currentUser);

        UserRepository userRepository = userRepository(currentUser);
        StudentApplicationRepository applicationRepository = studentApplicationRepository(ownedApplication);
        JobOpportunityRepository jobRepository = emptyJobRepository();

        UserContextService userContextService = new UserContextService(userRepository);
        JobService jobService = new JobService(jobRepository, userContextService, new MapperConfig().modelMapper());
        return new ApplicationService(applicationRepository, jobService, userContextService);
    }

    private UserRepository userRepository(User user) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByEmail" -> Optional.ofNullable(user.getEmail().equals(args[0]) ? user : null);
                    case "existsByEmail" -> user.getEmail().equals(args[0]);
                    case "toString" -> "UserRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private StudentApplicationRepository studentApplicationRepository(Optional<StudentApplication> ownedApplication) {
        return (StudentApplicationRepository) Proxy.newProxyInstance(
                StudentApplicationRepository.class.getClassLoader(),
                new Class[]{StudentApplicationRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByIdAndStudentId" -> ownedApplication;
                    case "toString" -> "StudentApplicationRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "findAll" -> List.of();
                    case "count" -> 0L;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private JobOpportunityRepository emptyJobRepository() {
        return (JobOpportunityRepository) Proxy.newProxyInstance(
                JobOpportunityRepository.class.getClassLoader(),
                new Class[]{JobOpportunityRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> "JobOpportunityRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "findAll" -> List.of();
                    case "count" -> 0L;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private void setAuthenticatedUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), "ignored", List.of())
        );
    }

    private User createUser(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
