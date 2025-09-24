package com.pm.historyservice.repository;

import com.pm.historyservice.models.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface HistoryRepository extends JpaRepository<History,Long> {
    List<History> findAllByEmail(String email);
}
