package com.librishare.backend.modules.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.entity.Loan;
import com.librishare.backend.modules.loan.repository.LoanRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LoanRepository loanRepository;
    @Autowired private UserBookRepository userBookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;

    private User user;
    private Book book;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        userBookRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();

        user = userRepository.save(User.builder().firstName("Loaner").lastName("User").email("loan@test.com").build());
        book = bookRepository.save(Book.builder().title("Book to Loan").author("Author").build());

        userBook = userBookRepository.save(UserBook.builder()
                .user(user)
                .book(book)
                .status(ReadingStatus.READ) 
                .build());
    }

    @Test
    @DisplayName("Deve criar um empréstimo com sucesso")
    void createLoan_Success() throws Exception {
        LoanRequestDTO dto = new LoanRequestDTO();
        dto.setBookId(book.getId());
        dto.setBorrowerName("Amigo João");
        dto.setBorrowerEmail("joao@amigo.com");
        dto.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/v1/users/" + user.getId() + "/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.borrowerName", is("Amigo João")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Deve listar empréstimos do usuário")
    void getUserLoans_Success() throws Exception {
        loanRepository.save(Loan.builder()
                .userBook(userBook)
                .borrowerName("Maria")
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .status("ACTIVE")
                .build());

        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].borrowerName", is("Maria")));
    }

    @Test
    @DisplayName("Deve devolver um livro (Return Loan)")
    void returnLoan_Success() throws Exception {
        Loan loan = loanRepository.save(Loan.builder()
                .userBook(userBook)
                .borrowerName("Pedro")
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .status("ACTIVE")
                .build());

        mockMvc.perform(patch("/api/v1/users/" + user.getId() + "/loans/" + loan.getId() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate").exists());
    }
}