package com.librishare.backend.modules.history.service.impl;

import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.history.entity.UserHistory;
import com.librishare.backend.modules.history.repository.UserHistoryRepository;
import com.librishare.backend.modules.history.service.HistoryService;
import com.librishare.backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final UserHistoryRepository repository;
    private final ModelMapper mapper;

    @Override
    public void logAction(User user, String actionType, String description) {
        UserHistory history = UserHistory.builder()
                .user(user)
                .actionType(actionType)
                .description(description)
                .build();
        repository.save(history);
    }

    @Override
    public List<HistoryResponseDTO> getUserHistory(Long userId) {
        return repository.findTop3ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(h -> mapper.map(h, HistoryResponseDTO.class))
                .collect(Collectors.toList());
    }
}