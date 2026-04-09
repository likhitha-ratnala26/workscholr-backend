package com.workscholr.backend.repository;

import com.workscholr.backend.model.JobOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobOpportunityRepository extends JpaRepository<JobOpportunity, Long> {
    List<JobOpportunity> findByActiveTrueOrderByPostedAtDesc();

    List<JobOpportunity> findAllByOrderByPostedAtDesc();
}
