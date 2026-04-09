package com.workscholr.backend.service;

import com.workscholr.backend.dto.JobDtos.CreateJobRequest;
import com.workscholr.backend.dto.JobDtos.JobResponse;
import com.workscholr.backend.exception.ResourceNotFoundException;
import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.model.User;
import com.workscholr.backend.repository.JobOpportunityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobOpportunityRepository jobOpportunityRepository;
    private final UserContextService userContextService;
    private final ModelMapper modelMapper;

    public JobService(JobOpportunityRepository jobOpportunityRepository,
                      UserContextService userContextService,
                      ModelMapper modelMapper) {
        this.jobOpportunityRepository = jobOpportunityRepository;
        this.userContextService = userContextService;
        this.modelMapper = modelMapper;
    }

    public JobResponse createJob(CreateJobRequest request) {
        User admin = userContextService.getCurrentUser();

        JobOpportunity job = modelMapper.map(request, JobOpportunity.class);
        job.setActive(true);
        job.setPostedBy(admin);

        return map(jobOpportunityRepository.save(job));
    }

    public List<JobResponse> getAllActiveJobs() {
        return jobOpportunityRepository.findByActiveTrueOrderByPostedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    public List<JobResponse> getAllJobsForAdmin() {
        return jobOpportunityRepository.findAllByOrderByPostedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    public JobResponse getActiveJobById(Long id) {
        JobOpportunity job = jobOpportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!Boolean.TRUE.equals(job.getActive())) {
            throw new ResourceNotFoundException("Job not found");
        }
        return map(job);
    }

    public JobResponse getJobByIdForAdmin(Long id) {
        JobOpportunity job = jobOpportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return map(job);
    }

    public long countJobs() {
        return jobOpportunityRepository.count();
    }

    public JobOpportunity getEntityById(Long id) {
        return jobOpportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public void deactivateJob(Long id) {
        JobOpportunity job = getEntityById(id);
        if (Boolean.TRUE.equals(job.getActive())) {
            job.setActive(false);
            jobOpportunityRepository.save(job);
        }
    }

    public JobResponse updateJob(Long id, CreateJobRequest request) {
        JobOpportunity job = getEntityById(id);
        modelMapper.map(request, job);
        return map(jobOpportunityRepository.save(job));
    }

    private JobResponse map(JobOpportunity job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDepartment(),
                job.getDescription(),
                job.getHoursPerWeek(),
                job.getMonthlyStipend(),
                job.getLocation(),
                job.getActive(),
                job.getPostedAt(),
                job.getPostedBy().getFullName()
        );
    }
}
