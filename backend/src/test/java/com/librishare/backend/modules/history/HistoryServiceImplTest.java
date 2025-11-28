package com.librishare.backend.modules.history;

import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.history.entity.UserHistory;
import com.librishare.backend.modules.history.repository.UserHistoryRepository;
import com.librishare.backend.modules.history.service.impl.HistoryServiceImpl;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository; // Import Adicionado
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private UserHistoryRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Captor
    private ArgumentCaptor<UserHistory> historyCaptor;

    private User user;
    private UserHistory historyEntity;
    private HistoryResponseDTO historyDTO;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Bianca").build();

        historyEntity = UserHistory.builder()
                .id(1L)
                .user(user)
                .actionType("LOGIN")
                .description("Usuário logou")
                .build();

        historyDTO = new HistoryResponseDTO();
        historyDTO.setActionType("LOGIN");
        historyDTO.setDescription("Usuário logou");
    }

    // --- Log Action Tests ---

    @Test
    @DisplayName("Deve salvar uma ação no histórico corretamente")
    void logAction_Success() {
        historyService.logAction(user, "EMPRÉSTIMO", "Emprestou um livro");

        verify(repository, times(1)).save(historyCaptor.capture());

        UserHistory capturedHistory = historyCaptor.getValue();

        assertNotNull(capturedHistory);
        assertEquals(user, capturedHistory.getUser());
        assertEquals("EMPRÉSTIMO", capturedHistory.getActionType());
        assertEquals("Emprestou um livro", capturedHistory.getDescription());
    }

    // --- Get History Tests ---

    @Test
    @DisplayName("Deve retornar os top 3 históricos do usuário")
    void getUserHistory_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        List<UserHistory> historyList = Arrays.asList(historyEntity, historyEntity);

        when(repository.findTop3ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(historyList);
        when(mapper.map(any(UserHistory.class), eq(HistoryResponseDTO.class))).thenReturn(historyDTO);

        List<HistoryResponseDTO> result = historyService.getUserHistory(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("LOGIN", result.get(0).getActionType());

        verify(userRepository).existsById(1L);
        verify(repository).findTop3ByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver histórico")
    void getUserHistory_Empty() {
        when(userRepository.existsById(1L)).thenReturn(true);

        when(repository.findTop3ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        List<HistoryResponseDTO> result = historyService.getUserHistory(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("Deve lançar erro 404 se o usuário não existir")
    void getUserHistory_UserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                historyService.getUserHistory(99L)
        );

        verify(repository, never()).findTop3ByUserIdOrderByCreatedAtDesc(anyLong());
    }
}