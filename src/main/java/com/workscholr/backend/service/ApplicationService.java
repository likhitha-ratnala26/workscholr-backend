package com.workscholr.backend.service;

import com.workscholr.backend.dto.ApplicationDtos.ApplicationResponse;
import com.workscholr.backend.dto.ApplicationDtos.ApplyJobRequest;
import com.workscholr.backend.dto.ApplicationDtos.UpdateApplicationStatusRequest;
import com.workscholr.backend.exception.BadRequestException;
import com.workscholr.backend.exception.ResourceNotFoundException;
import com.workscholr.backend.model.ApplicationStatus;
import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.model.StudentApplication;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.StudentApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ApplicationService {

    private final StudentApplicationRepository applicationRepository;
    private final JobService jobService;
    private final UserContextService userContextService;
    private final ResumeStorageService resumeStorageService;

    public ApplicationService(StudentApplicationRepository applicationRepository,
                              JobService jobService,
                              UserContextService userContextService,
                              ResumeStorageService resumeStorageService) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.userContextService = userContextService;
        this.resumeStorageService = resumeStorageService;
    }

    public ApplicationResponse apply(ApplyJobRequest request, MultipartFile resumeFile) {
        User student = userContextService.getCurrentUser();
        JobOpportunity job = jobService.getEntityById(request.getJobId());

        if (!Boolean.TRUE.equals(job.getActive())) {
            throw new BadRequestException("This job is not active");
        }

        if (applicationRepository.existsByStudentIdAndJobOpportunityId(student.getId(), job.getId())) {
            throw new BadRequestException("You have already applied for this job");
        }

        ResumeStorageService.StoredResume storedResume = resumeStorageService.store(resumeFile);

        StudentApplication application = new StudentApplication();
        application.setStudent(student);
        application.setJobOpportunity(job);
        application.setPhone(request.getPhone().trim());
        application.setGender(request.getGender().trim());
        application.setAddress(request.getAddress().trim());
        application.setDateOfBirth(request.getDateOfBirth());
        application.setStatementOfPurpose(trimToNull(request.getStatementOfPurpose()));
        application.setResumeFileName(storedResume.originalFileName());
        application.setResumeStoredFileName(storedResume.storedFileName());
        application.setResumeContentType(storedResume.contentType());
        application.setResumeSizeBytes(storedResume.sizeBytes());
        application.setStatus(ApplicationStatus.PENDING);

        try {
            return map(applicationRepository.save(application));
        } catch (RuntimeException exception) {
            resumeStorageService.deleteQuietly(storedResume.storedFileName());
            throw exception;
        }
    }

    public List<ApplicationResponse> getMyApplications() {
        User student = userContextService.getCurrentUser();
        return applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public ApplicationResponse updateStatus(Long applicationId, UpdateApplicationStatusRequest request) {
        StudentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        String decisionNote = trimToNull(request.decisionNote());
        if (request.status() == ApplicationStatus.REJECTED && decisionNote == null) {
            throw new BadRequestException("Rejection reason is required.");
        }

        application.setStatus(request.status());
        application.setAdminDecisionNote(request.status() == ApplicationStatus.REJECTED ? decisionNote : null);
        return map(applicationRepository.save(application));
    }

    public ResumeStorageService.ResumeDownload getResumeForAdmin(Long applicationId) {
        StudentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.hasResume()) {
            throw new ResourceNotFoundException("Resume not found for this application");
        }

        return resumeStorageService.load(
                application.getResumeStoredFileName(),
                application.getResumeFileName(),
                application.getResumeContentType()
        );
    }

    public StudentApplication getApprovedApplicationForCurrentStudent(Long applicationId) {
        User user = userContextService.getCurrentUser();
        StudentApplication application = applicationRepository.findByIdAndStudentId(applicationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Only approved applications can be used for work logs");
        }
        return application;
    }

    public StudentApplication getEntity(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public StudentApplication getEntityForCurrentStudent(Long applicationId) {
        User user = userContextService.getCurrentUser();
        return applicationRepository.findByIdAndStudentId(applicationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public long countAll() {
        return applicationRepository.count();
    }

    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.findAll().stream().filter(application -> application.getStatus() == status).count();
    }

    private ApplicationResponse map(StudentApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getStudent().getFullName(),
                application.getStudent().getEmail(),
                application.getJobOpportunity().getId(),
                application.getJobOpportunity().getTitle(),
                application.getJobOpportunity().getDepartment(),
                application.getPhone(),
                application.getGender(),
                application.getAddress(),
                application.getDateOfBirth(),
                application.getStatementOfPurpose(),
                application.getResumeFileName(),
                application.hasResume(),
                application.getAdminDecisionNote(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
