package com.pm.historyservice.repository;

import com.pm.historyservice.models.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchedHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findAllByEmail(String email);

    Optional<SearchHistory> findBySearch(String searched);
}
