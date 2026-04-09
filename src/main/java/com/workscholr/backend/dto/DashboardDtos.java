package com.workscholr.backend.dto;

import java.math.BigDecimal;

public class DashboardDtos {

    public record AdminDashboardResponse(
            long totalJobs,
            long totalApplications,
            long pendingApplications,
            long approvedApplications,
            long totalWorkLogs,
            BigDecimal approvedHours
    ) {
    }
}
