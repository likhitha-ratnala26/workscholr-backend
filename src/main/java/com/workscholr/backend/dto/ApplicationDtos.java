package com.workscholr.backend.dto;

import com.workscholr.backend.model.ApplicationStatus;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationDtos {

    public static class ApplyJobRequest {
        @NotNull
        private Long jobId;

        @NotBlank
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must contain 10 digits")
        private String phone;

        @NotBlank
        private String gender;

        @NotBlank
        @Size(max = 500)
        private String address;

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dateOfBirth;

        @Size(max = 1000)
        private String statementOfPurpose;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getStatementOfPurpose() {
            return statementOfPurpose;
        }

        public void setStatementOfPurpose(String statementOfPurpose) {
            this.statementOfPurpose = statementOfPurpose;
        }
    }

    public record UpdateApplicationStatusRequest(
            @NotNull ApplicationStatus status,
            @Size(max = 1000) String decisionNote
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
            String resumeFileName,
            boolean hasResume,
            String adminDecisionNote,
            ApplicationStatus status,
            LocalDateTime appliedAt
    ) {
    }
}
