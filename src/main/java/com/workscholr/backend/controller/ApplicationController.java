package com.workscholr.backend.controller;

import com.workscholr.backend.dto.ApplicationDtos.ApplicationResponse;
import com.workscholr.backend.dto.ApplicationDtos.ApplyJobRequest;
import com.workscholr.backend.dto.ApplicationDtos.UpdateApplicationStatusRequest;
import com.workscholr.backend.service.ApplicationService;
import com.workscholr.backend.service.ResumeStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@Valid @ModelAttribute ApplyJobRequest request,
                                     @RequestParam("resume") MultipartFile resume) {
        return applicationService.apply(request, resume);
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

    @GetMapping("/{id}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> getResume(@PathVariable Long id) {
        ResumeStorageService.ResumeDownload resume = applicationService.getResumeForAdmin(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.contentType()))
                .header(
                        "Content-Disposition",
                        ContentDisposition.inline()
                                .filename(resume.originalFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(resume.resource());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request);
    }
}
