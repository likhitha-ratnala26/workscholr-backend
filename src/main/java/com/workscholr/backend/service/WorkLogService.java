package com.workscholr.backend.service;

import com.workscholr.backend.dto.WorkLogDtos.CreateWorkLogRequest;
import com.workscholr.backend.dto.WorkLogDtos.UpdateWorkLogStatusRequest;
import com.workscholr.backend.dto.WorkLogDtos.WorkLogResponse;
import com.workscholr.backend.exception.ResourceNotFoundException;
import com.workscholr.backend.model.StudentApplication;
import com.workscholr.backend.model.User;
import com.workscholr.backend.model.WorkLog;
import com.workscholr.backend.model.WorkLogStatus;
import com.workscholr.backend.repository.WorkLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final ApplicationService applicationService;
    private final UserContextService userContextService;

    public WorkLogService(WorkLogRepository workLogRepository,
                          ApplicationService applicationService,
                          UserContextService userContextService) {
        this.workLogRepository = workLogRepository;
        this.applicationService = applicationService;
        this.userContextService = userContextService;
    }

    public WorkLogResponse create(CreateWorkLogRequest request) {
        StudentApplication application = applicationService.getApprovedApplicationForCurrentStudent(request.applicationId());

        WorkLog workLog = new WorkLog();
        workLog.setApplication(application);
        workLog.setWorkDate(request.workDate());
        workLog.setHoursWorked(request.hoursWorked());
        workLog.setWorkDescription(request.workDescription());
        workLog.setStatus(WorkLogStatus.PENDING);

        return map(workLogRepository.save(workLog));
    }

    public List<WorkLogResponse> getMyWorkLogs() {
        User student = userContextService.getCurrentUser();
        return workLogRepository.findByApplicationStudentIdOrderBySubmittedAtDesc(student.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public List<WorkLogResponse> getAll() {
        return workLogRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public WorkLogResponse updateStatus(Long workLogId, UpdateWorkLogStatusRequest request) {
        WorkLog workLog = workLogRepository.findById(workLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found"));
        workLog.setStatus(request.status());
        return map(workLogRepository.save(workLog));
    }

    public long countAll() {
        return workLogRepository.count();
    }

    public BigDecimal getApprovedHours() {
        return workLogRepository.findAll().stream()
                .filter(workLog -> workLog.getStatus() == WorkLogStatus.APPROVED)
                .map(WorkLog::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private WorkLogResponse map(WorkLog workLog) {
        return new WorkLogResponse(
                workLog.getId(),
                workLog.getApplication().getId(),
                workLog.getApplication().getStudent().getFullName(),
                workLog.getApplication().getJobOpportunity().getTitle(),
                workLog.getWorkDate(),
                workLog.getHoursWorked(),
                workLog.getWorkDescription(),
                workLog.getStatus(),
                workLog.getSubmittedAt()
        );
    }
}
