package com.librishare.backend.modules.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    private BookRequestDTO createValidBookDTO(String isbn, String googleId) {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Integration Test Book");
        dto.setAuthor("Test Author");
        dto.setIsbn(isbn);
        dto.setGoogleBooksId(googleId);
        return dto;
    }

    @Test
    @DisplayName("Deve criar um livro no catálogo com sucesso")
    void createBookInCatalog_Success() throws Exception {
        BookRequestDTO dto = createValidBookDTO("9780061122415", "googleId1");

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.isbn").value("9780061122415"));
    }

    @Test
    @DisplayName("Deve falhar ao criar livro com ISBN duplicado")
    void createBookInCatalog_DuplicateIsbn_Fails() throws Exception {
        bookRepository.save(Book.builder().title("Book 1").author("Author").isbn("9780061122415").build());
        BookRequestDTO dto = createValidBookDTO("9780061122415", "googleId2");

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Livro já cadastrado no catálogo com este ISBN: 9780061122415"));
    }
}