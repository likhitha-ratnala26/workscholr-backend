package com.workscholr.backend.repository;

import com.workscholr.backend.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByApplicationStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<WorkLog> findByApplicationIdOrderBySubmittedAtDesc(Long applicationId);
}
