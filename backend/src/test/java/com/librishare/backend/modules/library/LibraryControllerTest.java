package com.librishare.backend.modules.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.library.controller.LibraryController;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.service.LibraryService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private LibraryController libraryController;

    private ObjectMapper objectMapper;

    private UserBookResponse userBookResponse;
    private AddBookRequest addBookRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(libraryController)
                .setControllerAdvice(new TestControllerAdvice())
                .build();

        addBookRequest = new AddBookRequest();
        addBookRequest.setBookId(1L);
        addBookRequest.setStatus(ReadingStatus.WANT_TO_READ);

        userBookResponse = new UserBookResponse();
        userBookResponse.setId(10L);
        userBookResponse.setTitle("Clean Code");
        userBookResponse.setStatus(ReadingStatus.WANT_TO_READ);
        userBookResponse.setCurrentPage(0);
        userBookResponse.setRating(0);
    }

    // --- Add Book Tests (POST) ---

    @Test
    @DisplayName("Deve adicionar livro à estante com sucesso (201)")
    void addBookToLibrary_Success() throws Exception {
        when(libraryService.addBookToLibrary(eq(1L), any(AddBookRequest.class)))
                .thenReturn(userBookResponse);

        mockMvc.perform(post("/api/v1/users/{userId}/library", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.status", is("WANT_TO_READ")));
    }

    // --- Get Library Tests (GET) ---

    @Test
    @DisplayName("Deve listar a estante do usuário (200)")
    void getUserLibrary_Success() throws Exception {
        List<UserBookResponse> library = Collections.singletonList(userBookResponse);
        when(libraryService.getUserLibrary(1L)).thenReturn(library);

        mockMvc.perform(get("/api/v1/users/{userId}/library", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    // --- Remove Book Tests (DELETE) ---

    @Test
    @DisplayName("Deve remover livro da estante (204)")
    void removeBookFromLibrary_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}/library/{userBookId}", 1L, 10L))
                .andExpect(status().isNoContent());

        verify(libraryService).removeBookFromLibrary(1L, 10L);
    }

    // --- Update Status Tests (PATCH) ---

    @Test
    @DisplayName("Deve atualizar status de leitura com sucesso (200)")
    void updateBookStatus_Success() throws Exception {
        Map<String, String> statusUpdate = Map.of("status", "READING");

        UserBookResponse updatedResponse = new UserBookResponse();
        updatedResponse.setStatus(ReadingStatus.READING);

        when(libraryService.updateBookStatus(1L, 10L, ReadingStatus.READING))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/users/1/library/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READING")));
    }

    @Test
    @DisplayName("Deve retornar 400 se o status enviado for inválido (Enum inexistente)")
    void updateBookStatus_InvalidEnum() throws Exception {
        Map<String, String> invalidUpdate = Map.of("status", "LENDO_AGORA");

        mockMvc.perform(patch("/api/v1/users/1/library/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status inválido. Use WANT_TO_READ, READING, ou READ."));
    }

    // --- Update Progress Tests (PATCH) ---

    @Test
    @DisplayName("Deve atualizar progresso de páginas (200)")
    void updateBookProgress_Success() throws Exception {
        Map<String, Integer> progressUpdate = Map.of("currentPage", 50);

        userBookResponse.setCurrentPage(50);
        when(libraryService.updateBookProgress(1L, 10L, 50)).thenReturn(userBookResponse);

        mockMvc.perform(patch("/api/v1/users/1/library/10/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage", is(50)));
    }

    @Test
    @DisplayName("Deve retornar 400 se a página for negativa")
    void updateBookProgress_NegativePage() throws Exception {
        Map<String, Integer> invalidProgress = Map.of("currentPage", -5);

        mockMvc.perform(patch("/api/v1/users/1/library/10/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProgress)))
                .andExpect(status().isBadRequest());
    }

    // --- Update Rating Tests (PATCH) ---

    @Test
    @DisplayName("Deve atualizar avaliação (200)")
    void updateBookRating_Success() throws Exception {
        Map<String, Integer> ratingUpdate = Map.of("rating", 5);
        userBookResponse.setRating(5);

        when(libraryService.updateBookRating(1L, 10L, 5)).thenReturn(userBookResponse);

        mockMvc.perform(patch("/api/v1/users/1/library/10/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(5)));
    }

    @Test
    @DisplayName("Deve retornar 400 se faltar o campo rating")
    void updateBookRating_MissingField() throws Exception {
        Map<String, Integer> emptyMap = new HashMap<>(); // JSON vazio {}

        mockMvc.perform(patch("/api/v1/users/1/library/10/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O campo 'rating' é obrigatório."));
    }

    // --- Update Review Tests (PATCH) ---

    @Test
    @DisplayName("Deve atualizar resenha (200)")
    void updateBookReview_Success() throws Exception {
        Map<String, String> reviewUpdate = Map.of("review", "Muito bom!");
        userBookResponse.setReview("Muito bom!");

        when(libraryService.updateBookReview(1L, 10L, "Muito bom!")).thenReturn(userBookResponse);

        mockMvc.perform(patch("/api/v1/users/1/library/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review", is("Muito bom!")));
    }

    @Test
    @DisplayName("Deve retornar 400 se faltar o campo review")
    void updateBookReview_MissingField() throws Exception {
        Map<String, String> emptyMap = new HashMap<>(); // JSON vazio {}

        mockMvc.perform(patch("/api/v1/users/1/library/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O campo 'review' é obrigatório."));
    }

    // --- Stats Test ---

    @Test
    @DisplayName("Deve retornar estatísticas (200)")
    void getLibraryStats_Success() throws Exception {
        UserLibraryStatsDTO stats = new UserLibraryStatsDTO();
        stats.setTotalBooks(10);
        stats.setBooksRead(5);

        when(libraryService.getUserLibraryStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/users/1/library/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks", is(10)));
    }

    /**
     * Classe interna auxiliar para capturar as exceções lançadas manualmente na Controller
     * e transformá-las em respostas HTTP 400 para os testes passarem corretamente.
     */
    @RestControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Void> handleNotFound(ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}