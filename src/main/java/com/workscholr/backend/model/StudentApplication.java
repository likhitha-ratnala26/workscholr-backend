package com.workscholr.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_applications")
public class StudentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private JobOpportunity jobOpportunity;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 1000)
    private String statementOfPurpose;

    @Column(length = 255)
    private String resumeFileName;

    @Column(length = 255)
    private String resumeStoredFileName;

    @Column(length = 100)
    private String resumeContentType;

    private Long resumeSizeBytes;

    @Column(length = 1000)
    private String adminDecisionNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    void prePersist() {
        appliedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public JobOpportunity getJobOpportunity() {
        return jobOpportunity;
    }

    public void setJobOpportunity(JobOpportunity jobOpportunity) {
        this.jobOpportunity = jobOpportunity;
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

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getResumeStoredFileName() {
        return resumeStoredFileName;
    }

    public void setResumeStoredFileName(String resumeStoredFileName) {
        this.resumeStoredFileName = resumeStoredFileName;
    }

    public String getResumeContentType() {
        return resumeContentType;
    }

    public void setResumeContentType(String resumeContentType) {
        this.resumeContentType = resumeContentType;
    }

    public Long getResumeSizeBytes() {
        return resumeSizeBytes;
    }

    public void setResumeSizeBytes(Long resumeSizeBytes) {
        this.resumeSizeBytes = resumeSizeBytes;
    }

    public String getAdminDecisionNote() {
        return adminDecisionNote;
    }

    public void setAdminDecisionNote(String adminDecisionNote) {
        this.adminDecisionNote = adminDecisionNote;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public boolean hasResume() {
        return resumeStoredFileName != null && !resumeStoredFileName.isBlank();
    }
}
