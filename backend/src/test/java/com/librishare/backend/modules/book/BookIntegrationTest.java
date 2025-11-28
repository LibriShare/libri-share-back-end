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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("Deve criar um livro no catálogo")
    void createBook_Success() throws Exception {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Clean Architecture");
        dto.setAuthor("Robert C. Martin");
        dto.setIsbn("9780134494166");
        dto.setPrice(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Clean Architecture"));
    }

    @Test
    @DisplayName("Deve impedir criação de livro com ISBN duplicado")
    void createBook_DuplicateIsbn() throws Exception {
        bookRepository.save(Book.builder()
                .title("Livro A").author("Autor A").isbn("1234567890123").build());

        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Livro B");
        dto.setAuthor("Autor B");
        dto.setIsbn("1234567890123");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve listar livros do catálogo")
    void getAllBooks_Success() throws Exception {
        bookRepository.save(Book.builder().title("Book 1").author("Auth 1").build());
        bookRepository.save(Book.builder().title("Book 2").author("Auth 2").build());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}