package com.workscholr.backend.controller;

import com.workscholr.backend.dto.ApplicationDtos.ApplicationResponse;
import com.workscholr.backend.dto.ApplicationDtos.ApplyJobRequest;
import com.workscholr.backend.dto.ApplicationDtos.UpdateApplicationStatusRequest;
import com.workscholr.backend.service.ApplicationService;
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
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@Valid @RequestBody ApplyJobRequest request) {
        return applicationService.apply(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationResponse> getMyApplications() {
        return applicationService.getMyApplications();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApplicationResponse> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request);
    }
}
