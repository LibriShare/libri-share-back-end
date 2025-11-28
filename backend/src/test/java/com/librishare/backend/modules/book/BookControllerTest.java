package com.librishare.backend.modules.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.controller.BookController;
import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private ObjectMapper objectMapper;

    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;

    @BeforeEach
    void setUp() {
        // Inicializa o Jackson manualmente pois não temos contexto Spring
        objectMapper = new ObjectMapper();

        // Configura o MockMvc no modo "Standalone" (apenas essa controller)
        // DICA: Se você tiver um GlobalExceptionHandler, adicione .setControllerAdvice(new GlobalExceptionHandler())
        // antes do .build() para que os erros 404/409 sejam mapeados corretamente.
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .build();

        // Setup dos dados
        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Sapiens");
        bookRequestDTO.setAuthor("Yuval Harari");
        bookRequestDTO.setIsbn("1234567890123");
        bookRequestDTO.setGoogleBooksId("googleId123");

        bookResponseDTO = new BookResponseDTO();
        bookResponseDTO.setId(1L);
        bookResponseDTO.setTitle("Sapiens");
        bookResponseDTO.setAuthor("Yuval Harari");
        bookResponseDTO.setIsbn("1234567890123");
        bookResponseDTO.setGoogleBooksId("googleId123");
    }

    // --- Create Tests ---

    @Test
    @DisplayName("Deve criar um livro no catálogo com sucesso (Retorna 201)")
    void createBookInCatalog_Success() throws Exception {
        when(bookService.createBookInCatalog(any(BookRequestDTO.class))).thenReturn(bookResponseDTO);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Sapiens")));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar criar com dados inválidos (Retorna 400)")
    void createBookInCatalog_InvalidData() throws Exception {
        BookRequestDTO invalidDTO = new BookRequestDTO();
        // Como estamos em standalone, a validação @Valid funciona se o Hibernate Validator estiver no classpath

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar conflito se livro já existe (Retorna 409)")
    void createBookInCatalog_Duplicate() throws Exception {
        when(bookService.createBookInCatalog(any(BookRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Livro duplicado"));

        // OBS: Para isso passar, sua DuplicateResourceException deve ter @ResponseStatus(HttpStatus.CONFLICT)
        // OU você deve configurar o setControllerAdvice no setUp.
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isConflict());
    }

    // --- List Tests ---

    @Test
    @DisplayName("Deve retornar todos os livros (Retorna 200)")
    void getAllBooks_Success() throws Exception {
        List<BookResponseDTO> books = Collections.singletonList(bookResponseDTO);
        when(bookService.findAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Sapiens")));
    }

    // --- Get By ID Tests ---

    @Test
    @DisplayName("Deve retornar um livro por ID com sucesso (Retorna 200)")
    void getBookById_Success() throws Exception {
        when(bookService.findBookById(1L)).thenReturn(bookResponseDTO);

        mockMvc.perform(get("/api/v1/books/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Sapiens")));
    }

    @Test
    @DisplayName("Deve retornar 404 quando livro não encontrado por ID")
    void getBookById_NotFound() throws Exception {
        when(bookService.findBookById(1L))
                .thenThrow(new ResourceNotFoundException("Não encontrado"));

        mockMvc.perform(get("/api/v1/books/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    // --- Get By Google ID Tests ---

    @Test
    @DisplayName("Deve retornar um livro por Google ID com sucesso (Retorna 200)")
    void getBookByGoogleId_Success() throws Exception {
        when(bookService.findByGoogleBooksId("googleId123"))
                .thenReturn(Optional.of(bookResponseDTO));

        mockMvc.perform(get("/api/v1/books/google/{googleId}", "googleId123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleBooksId", is("googleId123")));
    }

    @Test
    @DisplayName("Deve retornar 404 quando livro não encontrado por Google ID")
    void getBookByGoogleId_NotFound() throws Exception {
        when(bookService.findByGoogleBooksId("invalido"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/books/google/{googleId}", "invalido"))
                .andExpect(status().isNotFound());
    }

    // --- Update Tests ---

    @Test
    @DisplayName("Deve atualizar um livro com sucesso (Retorna 200)")
    void updateBook_Success() throws Exception {
        BookRequestDTO updateRequest = new BookRequestDTO();
        updateRequest.setTitle("Sapiens: Uma Breve História");

        updateRequest.setAuthor("Yuval Harari");
        updateRequest.setIsbn("1234567890123");

        BookResponseDTO updatedResponse = new BookResponseDTO();
        updatedResponse.setId(1L);
        updatedResponse.setTitle("Sapiens: Uma Breve História");
        updatedResponse.setAuthor("Yuval Harari");

        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Sapiens: Uma Breve História")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar livro inexistente")
    void updateBook_NotFound() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Não encontrado"));

        mockMvc.perform(put("/api/v1/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isNotFound());
    }
}