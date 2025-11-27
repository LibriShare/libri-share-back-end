package com.librishare.backend.modules.history.service;

import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.user.entity.User;
import java.util.List;

public interface HistoryService {
    void logAction(User user, String actionType, String description);
    List<HistoryResponseDTO> getUserHistory(Long userId);
}