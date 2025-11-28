package com.librishare.backend.modules.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.loan.controller.LoanController;
import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;
import com.librishare.backend.modules.loan.service.LoanService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    private ObjectMapper objectMapper;

    private LoanRequestDTO loanRequestDTO;
    private LoanResponseDTO loanResponseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(loanController)
                .setControllerAdvice(new LoanTestControllerAdvice())
                .build();

        // --- Dados de Teste ---
        loanRequestDTO = new LoanRequestDTO();
        loanRequestDTO.setBookId(10L);
        loanRequestDTO.setBorrowerName("Amigo João");
        loanRequestDTO.setDueDate(LocalDate.now().plusDays(15));

        loanResponseDTO = new LoanResponseDTO();
        loanResponseDTO.setId(1L);
        loanResponseDTO.setBookTitle("O Senhor dos Anéis");
        loanResponseDTO.setBorrowerName("Amigo João");
        loanResponseDTO.setStatus("LOANED");
    }

    // --- Create Loan (POST) ---

    @Test
    @DisplayName("Deve registrar empréstimo com sucesso (201)")
    void createLoan_Success() throws Exception {
        when(loanService.createLoan(eq(1L), any(LoanRequestDTO.class))).thenReturn(loanResponseDTO);

        mockMvc.perform(post("/api/v1/users/{userId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.borrowerName", is("Amigo João")));
    }

    @Test
    @DisplayName("Deve retornar 400 se dados inválidos (ex: sem nome do mutuário)")
    void createLoan_InvalidData() throws Exception {
        LoanRequestDTO invalidDto = new LoanRequestDTO();

        mockMvc.perform(post("/api/v1/users/{userId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 se livro não encontrado na estante")
    void createLoan_BookNotFound() throws Exception {
        when(loanService.createLoan(eq(1L), any(LoanRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Livro não disponível"));

        mockMvc.perform(post("/api/v1/users/{userId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequestDTO)))
                .andExpect(status().isNotFound()); // Capturado pelo ControllerAdvice
    }

    // --- Get User Loans (GET) ---

    @Test
    @DisplayName("Deve listar empréstimos do usuário (200)")
    void getUserLoans_Success() throws Exception {
        List<LoanResponseDTO> loans = Collections.singletonList(loanResponseDTO);
        when(loanService.getLoansByUserId(1L)).thenReturn(loans);

        mockMvc.perform(get("/api/v1/users/{userId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookTitle", is("O Senhor dos Anéis")));
    }

    // --- Return Loan (PATCH) ---

    @Test
    @DisplayName("Deve registrar devolução com sucesso (200)")
    void returnLoan_Success() throws Exception {
        LoanResponseDTO returnedDto = new LoanResponseDTO();
        returnedDto.setId(1L);
        returnedDto.setStatus("RETURNED");

        when(loanService.returnLoan(50L)).thenReturn(returnedDto);

        // O userId (1L) está na URL mas a service usa apenas o loanId (50L) no seu código atual
        mockMvc.perform(patch("/api/v1/users/{userId}/loans/{loanId}/return", 1L, 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")));
        
        verify(loanService).returnLoan(50L);
    }

    @Test
    @DisplayName("Deve retornar 404 se empréstimo não encontrado para devolução")
    void returnLoan_NotFound() throws Exception {
        when(loanService.returnLoan(99L))
                .thenThrow(new ResourceNotFoundException("Empréstimo não encontrado"));

        mockMvc.perform(patch("/api/v1/users/{userId}/loans/{loanId}/return", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    /**
     * Tratamento de exceções local para o teste funcionar sem contexto Spring completo
     */
    @RestControllerAdvice
    static class LoanTestControllerAdvice {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Void> handleNotFound() {
            return ResponseEntity.notFound().build();
        }

        // Caso tenha validações de data (ex: devolução antes do empréstimo) que lancem IllegalArgumentException
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Void> handleBadRequest() {
            return ResponseEntity.badRequest().build();
        }
    }
}