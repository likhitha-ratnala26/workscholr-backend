package com.workscholr.backend.controller;

import com.workscholr.backend.dto.WorkLogDtos.CreateWorkLogRequest;
import com.workscholr.backend.dto.WorkLogDtos.UpdateWorkLogStatusRequest;
import com.workscholr.backend.dto.WorkLogDtos.WorkLogResponse;
import com.workscholr.backend.service.WorkLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/worklogs")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkLogResponse create(@Valid @RequestBody CreateWorkLogRequest request) {
        return workLogService.create(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<WorkLogResponse> getMyWorkLogs() {
        return workLogService.getMyWorkLogs();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WorkLogResponse> getAll() {
        return workLogService.getAll();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkLogResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateWorkLogStatusRequest request) {
        return workLogService.updateStatus(id, request);
    }
}
