package com.workscholr.backend.service;

import com.workscholr.backend.config.MapperConfig;
import com.workscholr.backend.dto.JobDtos.JobResponse;
import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.repository.JobOpportunityRepository;
import com.workscholr.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceTest {

    @Test
    void getAllJobsForAdminIncludesInactiveJobs() {
        JobOpportunityRepository jobRepository = mock(JobOpportunityRepository.class);
        UserContextService userContextService = new UserContextService(mock(UserRepository.class));

        JobOpportunity activeJob = createJob(1L, "Campus Assistant", true);
        JobOpportunity inactiveJob = createJob(2L, "Old Library Role", false);

        when(jobRepository.findAllByOrderByPostedAtDesc()).thenReturn(List.of(activeJob, inactiveJob));

        JobService jobService = new JobService(jobRepository, userContextService, new MapperConfig().modelMapper());
        List<JobResponse> jobs = jobService.getAllJobsForAdmin();

        assertEquals(2, jobs.size());
        assertEquals(true, jobs.get(0).active());
        assertEquals(false, jobs.get(1).active());
    }

    @Test
    void deactivateJobMarksActiveJobAsInactive() {
        JobOpportunityRepository jobRepository = mock(JobOpportunityRepository.class);
        UserContextService userContextService = new UserContextService(mock(UserRepository.class));
        JobOpportunity job = createJob(5L, "Lab Assistant", true);

        when(jobRepository.findById(5L)).thenReturn(java.util.Optional.of(job));
        when(jobRepository.save(any(JobOpportunity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobService jobService = new JobService(jobRepository, userContextService, new MapperConfig().modelMapper());
        jobService.deactivateJob(5L);

        assertFalse(job.getActive());
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    void updateJobChangesEditableFields() {
        JobOpportunityRepository jobRepository = mock(JobOpportunityRepository.class);
        UserContextService userContextService = new UserContextService(mock(UserRepository.class));
        JobOpportunity job = createJob(7L, "Old Title", true);

        when(jobRepository.findById(7L)).thenReturn(java.util.Optional.of(job));
        when(jobRepository.save(any(JobOpportunity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobService jobService = new JobService(jobRepository, userContextService, new MapperConfig().modelMapper());
        JobResponse updated = jobService.updateJob(
                7L,
                new com.workscholr.backend.dto.JobDtos.CreateJobRequest(
                        "Updated Title",
                        "Library",
                        "Updated description",
                        18,
                        java.math.BigDecimal.valueOf(9000),
                        "Central Library"
                )
        );

        assertEquals("Updated Title", updated.title());
        assertEquals("Library", updated.department());
        assertEquals(18, updated.hoursPerWeek());
        assertEquals("Central Library", updated.location());
    }

    private JobOpportunity createJob(Long id, String title, boolean active) {
        JobOpportunity job = new JobOpportunity();
        job.setId(id);
        job.setTitle(title);
        job.setDepartment("Admin");
        job.setDescription("Sample description");
        job.setHoursPerWeek(10);
        job.setMonthlyStipend(java.math.BigDecimal.valueOf(5000));
        job.setLocation("Campus");
        job.setActive(active);

        com.workscholr.backend.model.User admin = new com.workscholr.backend.model.User();
        admin.setFullName("System Admin");
        job.setPostedBy(admin);
        return job;
    }
}
