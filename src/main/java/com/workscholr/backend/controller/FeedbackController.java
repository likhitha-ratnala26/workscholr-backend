package com.workscholr.backend.controller;

import com.workscholr.backend.dto.FeedbackDtos.CreateFeedbackRequest;
import com.workscholr.backend.dto.FeedbackDtos.FeedbackResponse;
import com.workscholr.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse create(@Valid @RequestBody CreateFeedbackRequest request) {
        return feedbackService.create(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<FeedbackResponse> getMyFeedback() {
        return feedbackService.getMyFeedback();
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public List<FeedbackResponse> getByApplication(@PathVariable Long applicationId) {
        return feedbackService.getByApplication(applicationId);
    }
}
