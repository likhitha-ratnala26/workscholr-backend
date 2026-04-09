package com.workscholr.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobDtos {

    public record CreateJobRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 100) String department,
            @NotBlank @Size(max = 1500) String description,
            @NotNull Integer hoursPerWeek,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal monthlyStipend,
            @Size(max = 150) String location
    ) {
    }

    public record JobResponse(
            Long id,
            String title,
            String department,
            String description,
            Integer hoursPerWeek,
            BigDecimal monthlyStipend,
            String location,
            Boolean active,
            LocalDateTime postedAt,
            String postedByName
    ) {
    }
}
