package com.workscholr.backend.repository;

import com.workscholr.backend.model.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {
    List<StudentApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    boolean existsByStudentIdAndJobOpportunityId(Long studentId, Long jobId);

    Optional<StudentApplication> findByIdAndStudentId(Long applicationId, Long studentId);
}
