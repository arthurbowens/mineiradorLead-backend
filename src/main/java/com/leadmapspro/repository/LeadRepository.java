package com.leadmapspro.repository;

import com.leadmapspro.domain.Lead;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findBySearchHistory_IdOrderByCreatedAtAsc(UUID searchHistoryId);

    Optional<Lead> findByIdAndSearchHistory_User_Id(UUID id, UUID userId);
}
