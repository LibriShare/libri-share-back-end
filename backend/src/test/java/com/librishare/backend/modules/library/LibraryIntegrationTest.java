package com.librishare.backend.modules.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
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

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LibraryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserBookRepository userBookRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        userBookRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();

        user = userRepository.save(User.builder().firstName("Lib").lastName("User").email("lib@test.com").build());
        book = bookRepository.save(Book.builder().title("Lib Book").author("Lib Author").isbn("123").build());
    }

    @Test
    @DisplayName("Deve adicionar livro à estante")
    void addBookToLibrary() throws Exception {
        AddBookRequest req = new AddBookRequest(book.getId(), ReadingStatus.WANT_TO_READ);

        mockMvc.perform(post("/api/v1/users/" + user.getId() + "/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("WANT_TO_READ")))
                .andExpect(jsonPath("$.title", is("Lib Book")));
    }

    @Test
    @DisplayName("Deve atualizar o status de leitura")
    void updateBookStatus() throws Exception {
        UserBook ub = userBookRepository.save(UserBook.builder().user(user).book(book).status(ReadingStatus.WANT_TO_READ).build());

        Map<String, String> update = Map.of("status", "READING");

        mockMvc.perform(patch("/api/v1/users/" + user.getId() + "/library/" + ub.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READING")));
    }
}