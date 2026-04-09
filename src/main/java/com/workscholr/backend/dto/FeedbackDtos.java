package com.workscholr.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class FeedbackDtos {

    public record CreateFeedbackRequest(
            @NotNull Long applicationId,
            @NotBlank @Size(max = 1000) String comments,
            @NotNull @Min(1) @Max(5) Integer rating
    ) {
    }

    public record FeedbackResponse(
            Long id,
            Long applicationId,
            String studentName,
            String jobTitle,
            String adminName,
            String comments,
            Integer rating,
            LocalDateTime createdAt
    ) {
    }
}
