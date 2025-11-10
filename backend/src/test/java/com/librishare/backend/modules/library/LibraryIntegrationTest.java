package com.librishare.backend.modules.library;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserBookRepository userBookRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        userBookRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();

        testUser = User.builder()
                .email("library.user@test.com")
                .cpf("11122233344")
                .firstName("Library")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(testUser);

        testBook = Book.builder()
                .title("Library Test Book")
                .author("Author")
                .isbn("1112223334455")
                .build();
        bookRepository.save(testBook);
    }

    @Test
    @DisplayName("Deve adicionar um livro à biblioteca do usuário")
    void addBookToLibrary_Success() throws Exception {
        AddBookRequest dto = new AddBookRequest(testBook.getId(), ReadingStatus.WANT_TO_READ);

        mockMvc.perform(post("/api/v1/users/" + testUser.getId() + "/library")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.status").value("WANT_TO_READ"));
    }

    @Test
    @DisplayName("Deve buscar a biblioteca de um usuário")
    void getUserLibrary_Success() throws Exception {
        addBookToLibrary_Success();

        mockMvc.perform(get("/api/v1/users/" + testUser.getId() + "/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookId", is(testBook.getId().intValue())));
    }

    @Test
    @DisplayName("Deve remover livro que não pertence ao usuário")
    void removeBookFromLibrary_NotOwner_Fails() throws Exception {
        // Cria um segundo usuário (completo)
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .cpf("55544433322")
                .password(passwordEncoder.encode("p"))
                .firstName("Other") // Campo obrigatório
                .lastName("User") // Campo obrigatório
                .build());

        UserBook otherUserBook = userBookRepository.save(UserBook.builder().user(otherUser).book(testBook).status(ReadingStatus.READ).build());

        // Tenta remover o livro de "otherUser" usando o ID do "testUser"
        mockMvc.perform(delete("/api/v1/users/" + testUser.getId() + "/library/" + otherUserBook.getId()))
                .andExpect(status().isNotFound());
    }
}