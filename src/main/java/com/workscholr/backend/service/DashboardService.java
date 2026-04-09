package com.workscholr.backend.service;

import com.workscholr.backend.dto.DashboardDtos.AdminDashboardResponse;
import com.workscholr.backend.model.ApplicationStatus;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final WorkLogService workLogService;

    public DashboardService(JobService jobService,
                            ApplicationService applicationService,
                            WorkLogService workLogService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.workLogService = workLogService;
    }

    public AdminDashboardResponse getAdminDashboard() {
        return new AdminDashboardResponse(
                jobService.countJobs(),
                applicationService.countAll(),
                applicationService.countByStatus(ApplicationStatus.PENDING),
                applicationService.countByStatus(ApplicationStatus.APPROVED),
                workLogService.countAll(),
                workLogService.getApprovedHours()
        );
    }
}
