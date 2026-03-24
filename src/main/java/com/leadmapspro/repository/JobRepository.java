package com.leadmapspro.repository;

import com.leadmapspro.domain.Job;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface JobRepository extends JpaRepository<Job, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Job> findTopByStatusOrderByCreatedAtAsc(String status);

    List<Job> findByStatusAndUpdatedAtBefore(String status, LocalDateTime before);
}
