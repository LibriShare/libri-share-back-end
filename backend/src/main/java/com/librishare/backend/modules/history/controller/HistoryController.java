package com.librishare.backend.modules.history.controller;

import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<HistoryResponseDTO>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(historyService.getUserHistory(userId));
    }
}