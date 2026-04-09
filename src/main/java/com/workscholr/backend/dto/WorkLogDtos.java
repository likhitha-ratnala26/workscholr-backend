package com.workscholr.backend.dto;

import com.workscholr.backend.model.WorkLogStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkLogDtos {

    public record CreateWorkLogRequest(
            @NotNull Long applicationId,
            @NotNull LocalDate workDate,
            @NotNull
            @DecimalMin(value = "0.5", message = "Hours must be at least 0.5")
            @DecimalMax(value = "24.0", message = "Hours cannot exceed 24")
            BigDecimal hoursWorked,
            @NotBlank @Size(max = 500) String workDescription
    ) {
    }

    public record UpdateWorkLogStatusRequest(
            @NotNull WorkLogStatus status
    ) {
    }

    public record WorkLogResponse(
            Long id,
            Long applicationId,
            String studentName,
            String jobTitle,
            LocalDate workDate,
            BigDecimal hoursWorked,
            String workDescription,
            WorkLogStatus status,
            LocalDateTime submittedAt
    ) {
    }
}
