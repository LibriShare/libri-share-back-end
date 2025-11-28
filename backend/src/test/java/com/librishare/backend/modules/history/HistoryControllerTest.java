package com.librishare.backend.modules.history;

import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.history.controller.HistoryController;
import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.history.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private HistoryController historyController;

    private HistoryResponseDTO historyItem1;
    private HistoryResponseDTO historyItem2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(historyController)
                .setControllerAdvice(new HistoryTestControllerAdvice())
                .build();

        // Dados de teste
        historyItem1 = new HistoryResponseDTO();
        historyItem1.setActionType("EMPRÉSTIMO");
        historyItem1.setDescription("Emprestou o livro Clean Code");

        historyItem2 = new HistoryResponseDTO();
        historyItem2.setActionType("DEVOLUÇÃO");
        historyItem2.setDescription("Devolveu o livro Clean Code");
    }

    @Test
    @DisplayName("Deve retornar o histórico do usuário com sucesso (200)")
    void getUserHistory_Success() throws Exception {
        List<HistoryResponseDTO> historyList = Arrays.asList(historyItem1, historyItem2);

        when(historyService.getUserHistory(1L)).thenReturn(historyList);

        mockMvc.perform(get("/api/v1/users/{userId}/history", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].actionType", is("EMPRÉSTIMO")))
                .andExpect(jsonPath("$[1].actionType", is("DEVOLUÇÃO")));
    }

    @Test
    @DisplayName("Deve retornar lista vazia se usuário existe mas não tem histórico (200)")
    void getUserHistory_EmptyList() throws Exception {
        when(historyService.getUserHistory(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/{userId}/history", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Deve retornar 404 se o usuário não for encontrado")
    void getUserHistory_UserNotFound() throws Exception {
        when(historyService.getUserHistory(99L))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/api/v1/users/{userId}/history", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * ControllerAdvice local para capturar a ResourceNotFoundException
     * e garantir que o teste receba um 404 real.
     */
    @RestControllerAdvice
    static class HistoryTestControllerAdvice {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Void> handleNotFound() {
            return ResponseEntity.notFound().build();
        }
    }
}