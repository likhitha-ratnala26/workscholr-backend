package com.workscholr.backend.service;

import com.workscholr.backend.config.MapperConfig;
import com.workscholr.backend.model.ApplicationStatus;
import com.workscholr.backend.model.Feedback;
import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.StudentApplication;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.FeedbackRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getByApplicationChecksStudentOwnershipBeforeReturningFeedback() {
        User student = createUser(5L, "student@example.com", "Student One", Role.STUDENT);
        StudentApplication application = createApplication(12L, student, "Campus Assistant");
        Feedback feedback = createFeedback(3L, application, "Admin User", "Strong work", 5);

        FeedbackService feedbackService = createFeedbackService(student, Optional.of(application), List.of(feedback));

        var responses = feedbackService.getByApplication(12L);

        assertEquals(1, responses.size());
        assertEquals("Student One", responses.get(0).studentName());
        assertEquals("Campus Assistant", responses.get(0).jobTitle());
    }

    @Test
    void getByApplicationAllowsAdminLookupWithoutStudentOwnershipCheck() {
        User admin = createUser(1L, "admin@example.com", "Admin User", Role.ADMIN);
        User student = createUser(6L, "student2@example.com", "Student Two", Role.STUDENT);
        StudentApplication application = createApplication(9L, student, "Library Assistant");
        Feedback feedback = createFeedback(4L, application, "Admin User", "Needs improvement", 3);

        FeedbackService feedbackService = createFeedbackService(admin, Optional.empty(), List.of(feedback));

        var responses = feedbackService.getByApplication(9L);

        assertEquals(1, responses.size());
        assertEquals("Admin User", responses.get(0).adminName());
        assertEquals("Student Two", responses.get(0).studentName());
    }

    private FeedbackService createFeedbackService(User currentUser,
                                                  Optional<StudentApplication> ownedApplication,
                                                  List<Feedback> feedbacks) {
        setAuthenticatedUser(currentUser);

        UserRepository userRepository = userRepository(currentUser);
        StudentApplicationRepository applicationRepository = studentApplicationRepository(ownedApplication, feedbacks);
        JobOpportunityRepository jobRepository = emptyJobRepository();
        FeedbackRepository feedbackRepository = feedbackRepository(feedbacks);

        UserContextService userContextService = new UserContextService(userRepository);
        JobService jobService = new JobService(jobRepository, userContextService, new MapperConfig().modelMapper());
        ApplicationService applicationService = new ApplicationService(applicationRepository, jobService, userContextService);
        return new FeedbackService(feedbackRepository, applicationService, userContextService);
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

    private StudentApplicationRepository studentApplicationRepository(Optional<StudentApplication> ownedApplication,
                                                                     List<Feedback> feedbacks) {
        return (StudentApplicationRepository) Proxy.newProxyInstance(
                StudentApplicationRepository.class.getClassLoader(),
                new Class[]{StudentApplicationRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByIdAndStudentId" -> ownedApplication;
                    case "findById" -> findApplication(feedbacks, (Long) args[0]);
                    case "findAll" -> feedbacks.stream().map(Feedback::getApplication).toList();
                    case "count" -> (long) feedbacks.size();
                    case "toString" -> "StudentApplicationRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private FeedbackRepository feedbackRepository(List<Feedback> feedbacks) {
        return (FeedbackRepository) Proxy.newProxyInstance(
                FeedbackRepository.class.getClassLoader(),
                new Class[]{FeedbackRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByApplicationIdOrderByCreatedAtDesc" -> feedbacks.stream()
                            .filter(feedback -> feedback.getApplication().getId().equals(args[0]))
                            .toList();
                    case "toString" -> "FeedbackRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private JobOpportunityRepository emptyJobRepository() {
        return (JobOpportunityRepository) Proxy.newProxyInstance(
                JobOpportunityRepository.class.getClassLoader(),
                new Class[]{JobOpportunityRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findAll" -> List.of();
                    case "count" -> 0L;
                    case "toString" -> "JobOpportunityRepositoryProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private Optional<StudentApplication> findApplication(List<Feedback> feedbacks, Long applicationId) {
        return feedbacks.stream()
                .map(Feedback::getApplication)
                .filter(application -> application.getId().equals(applicationId))
                .findFirst();
    }

    private void setAuthenticatedUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), "ignored", List.of())
        );
    }

    private User createUser(Long id, String email, String fullName, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private StudentApplication createApplication(Long id, User student, String jobTitle) {
        JobOpportunity job = new JobOpportunity();
        job.setId(30L);
        job.setTitle(jobTitle);

        StudentApplication application = new StudentApplication();
        application.setId(id);
        application.setStudent(student);
        application.setJobOpportunity(job);
        application.setStatus(ApplicationStatus.APPROVED);
        return application;
    }

    private Feedback createFeedback(Long id,
                                    StudentApplication application,
                                    String adminName,
                                    String comments,
                                    int rating) {
        User admin = createUser(40L, "admin-feedback@example.com", adminName, Role.ADMIN);

        Feedback feedback = new Feedback();
        feedback.setId(id);
        feedback.setApplication(application);
        feedback.setAdmin(admin);
        feedback.setComments(comments);
        feedback.setRating(rating);
        return feedback;
    }
}
