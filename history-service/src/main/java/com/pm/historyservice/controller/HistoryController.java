package com.pm.historyservice.controller;

import com.pm.historyservice.dto.HistoryRequest;
import com.pm.historyservice.mapper.HistoryMapper;
import com.pm.historyservice.models.History;
import com.pm.historyservice.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryMapper historyMapper;
    private final HistoryService historyService;

    public HistoryController(HistoryMapper historyMapper, HistoryService historyService) {
        this.historyMapper = historyMapper;
        this.historyService = historyService;
    }

    @PostMapping
    @Operation(summary = "Save History", description = "Save History")
    public ResponseEntity<History> save(@RequestBody HistoryRequest historyrequest, Authentication authentication) {

        History history = historyMapper.convertHistoryRequestToHistory(historyrequest);
        history.setEmail(authentication.getName());

        return ResponseEntity
                .ok()
                .body(historyService.save(history));
    }

    @GetMapping
    public ResponseEntity<List<History>> findAll(Authentication authentication) {
        return ResponseEntity
                .ok()
                .body(historyService.findAllByEmail(authentication.getName()));
    }
}
