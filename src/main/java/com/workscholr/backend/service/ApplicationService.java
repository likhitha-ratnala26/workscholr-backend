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

import java.util.List;

@Service
public class ApplicationService {

    private final StudentApplicationRepository applicationRepository;
    private final JobService jobService;
    private final UserContextService userContextService;

    public ApplicationService(StudentApplicationRepository applicationRepository,
                              JobService jobService,
                              UserContextService userContextService) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.userContextService = userContextService;
    }

    public ApplicationResponse apply(ApplyJobRequest request) {
        User student = userContextService.getCurrentUser();
        JobOpportunity job = jobService.getEntityById(request.jobId());

        if (!Boolean.TRUE.equals(job.getActive())) {
            throw new BadRequestException("This job is not active");
        }

        if (applicationRepository.existsByStudentIdAndJobOpportunityId(student.getId(), job.getId())) {
            throw new BadRequestException("You have already applied for this job");
        }

        StudentApplication application = new StudentApplication();
        application.setStudent(student);
        application.setJobOpportunity(job);
        application.setPhone(request.phone());
        application.setGender(request.gender());
        application.setAddress(request.address());
        application.setDateOfBirth(request.dateOfBirth());
        application.setStatementOfPurpose(request.statementOfPurpose());
        application.setStatus(ApplicationStatus.PENDING);

        return map(applicationRepository.save(application));
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
        application.setStatus(request.status());
        return map(applicationRepository.save(application));
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
                application.getStatus(),
                application.getAppliedAt()
        );
    }
}
