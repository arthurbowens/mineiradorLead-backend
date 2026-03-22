package com.leadmapspro.repository;

import com.leadmapspro.domain.SearchHistory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    boolean existsByIdAndUser_Id(UUID id, UUID userId);

    Optional<SearchHistory> findByIdAndUser_Id(UUID id, UUID userId);

    Page<SearchHistory> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
