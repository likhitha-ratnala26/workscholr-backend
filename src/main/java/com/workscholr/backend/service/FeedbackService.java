package com.workscholr.backend.service;

import com.workscholr.backend.dto.FeedbackDtos.CreateFeedbackRequest;
import com.workscholr.backend.dto.FeedbackDtos.FeedbackResponse;
import com.workscholr.backend.exception.BadRequestException;
import com.workscholr.backend.model.ApplicationStatus;
import com.workscholr.backend.model.Feedback;
import com.workscholr.backend.model.Role;
import com.workscholr.backend.model.StudentApplication;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ApplicationService applicationService;
    private final UserContextService userContextService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           ApplicationService applicationService,
                           UserContextService userContextService) {
        this.feedbackRepository = feedbackRepository;
        this.applicationService = applicationService;
        this.userContextService = userContextService;
    }

    public FeedbackResponse create(CreateFeedbackRequest request) {
        StudentApplication application = applicationService.getEntity(request.applicationId());
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Feedback can be given only for approved applications");
        }

        User admin = userContextService.getCurrentUser();

        Feedback feedback = new Feedback();
        feedback.setApplication(application);
        feedback.setAdmin(admin);
        feedback.setComments(request.comments());
        feedback.setRating(request.rating());

        return map(feedbackRepository.save(feedback));
    }

    public List<FeedbackResponse> getMyFeedback() {
        User student = userContextService.getCurrentUser();
        return feedbackRepository.findByApplicationStudentIdOrderByCreatedAtDesc(student.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public List<FeedbackResponse> getByApplication(Long applicationId) {
        User user = userContextService.getCurrentUser();
        if (user.getRole() == Role.STUDENT) {
            applicationService.getEntityForCurrentStudent(applicationId);
        } else {
            applicationService.getEntity(applicationId);
        }

        return feedbackRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .map(this::map)
                .toList();
    }

    private FeedbackResponse map(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getApplication().getId(),
                feedback.getApplication().getStudent().getFullName(),
                feedback.getApplication().getJobOpportunity().getTitle(),
                feedback.getAdmin().getFullName(),
                feedback.getComments(),
                feedback.getRating(),
                feedback.getCreatedAt()
        );
    }
}
