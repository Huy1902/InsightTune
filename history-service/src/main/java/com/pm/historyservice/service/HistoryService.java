package com.pm.historyservice.service;

import com.pm.historyservice.models.History;
import com.pm.historyservice.repository.HistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
public class HistoryService {
    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public History save(History history) {
        return historyRepository.save(history);
    }

    public List<History> findAllByEmail(String email) {
        return historyRepository.findAllByEmail(email);
    }
}
