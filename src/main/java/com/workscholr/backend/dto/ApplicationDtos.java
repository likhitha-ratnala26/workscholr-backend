package com.workscholr.backend.dto;

import com.workscholr.backend.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationDtos {

    public record ApplyJobRequest(
            @NotNull Long jobId,
            @NotBlank @Pattern(regexp = "^\\d{10}$", message = "Phone number must contain 10 digits") String phone,
            @NotBlank String gender,
            @NotBlank @Size(max = 500) String address,
            @NotNull LocalDate dateOfBirth,
            @Size(max = 1000) String statementOfPurpose
    ) {
    }

    public record UpdateApplicationStatusRequest(
            @NotNull ApplicationStatus status
    ) {
    }

    public record ApplicationResponse(
            Long id,
            Long studentId,
            String studentName,
            String studentEmail,
            Long jobId,
            String jobTitle,
            String department,
            String phone,
            String gender,
            String address,
            LocalDate dateOfBirth,
            String statementOfPurpose,
            ApplicationStatus status,
            LocalDateTime appliedAt
    ) {
    }
}
