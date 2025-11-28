package com.librishare.backend.modules.loan;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.history.service.HistoryService;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;
import com.librishare.backend.modules.loan.entity.Loan;
import com.librishare.backend.modules.loan.repository.LoanRepository;
import com.librishare.backend.modules.loan.service.impl.LoanServiceImpl;
import com.librishare.backend.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User user;
    private Book book;
    private UserBook userBook;
    private Loan loan;
    private LoanRequestDTO loanRequestDTO;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Bianca").build();
        book = Book.builder().id(10L).title("Dom Quixote").build();

        userBook = UserBook.builder()
                .id(100L)
                .user(user)
                .book(book)
                .status(ReadingStatus.READ)
                .build();

        loanRequestDTO = new LoanRequestDTO();
        loanRequestDTO.setBookId(10L);
        loanRequestDTO.setBorrowerName("João da Silva");

        loan = Loan.builder()
                .id(1L)
                .userBook(userBook)
                .borrowerName("João da Silva")
                .status("ACTIVE")
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .build();
    }

    // --- Create Loan Tests ---

    @ParameterizedTest
    @CsvSource({
            "João da Silva, joao@email.com, Cuidar bem",
            "Maria Souza, maria@test.com, Devolver rápido",
            "Pedro, , Sem notas"
    })
    @DisplayName("Deve criar empréstimo com sucesso para diferentes dados de mutuário")
    void createLoan_Parameterized(String borrowerName, String email, String notes) {
        // Arrange
        LoanRequestDTO req = new LoanRequestDTO();
        req.setBookId(10L);
        req.setBorrowerName(borrowerName);
        req.setBorrowerEmail(email);
        req.setNotes(notes);

        Loan dynamicLoan = Loan.builder()
                .userBook(userBook)
                .borrowerName(borrowerName)
                .borrowerEmail(email)
                .status("ACTIVE")
                .build();

        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(loanRepository.existsByUserBookIdAndStatus(100L, "ACTIVE")).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(dynamicLoan);

        // Act
        LoanResponseDTO result = loanService.createLoan(1L, req);

        // Assert
        assertNotNull(result);
        assertEquals(borrowerName, result.getBorrowerName());
        verify(historyService).logAction(eq(user), eq("EMPRÉSTIMO"), anyString());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    @DisplayName("Deve lançar erro se o livro não estiver na biblioteca")
    void createLoan_BookNotFound() {
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> loanService.createLoan(1L, loanRequestDTO));
    }

    @Test
    @DisplayName("Deve lançar erro se status do livro for inválido")
    void createLoan_InvalidStatus() {
        userBook.setStatus(ReadingStatus.READING);
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(1L, loanRequestDTO));
    }

    @Test
    @DisplayName("Deve definir data de devolução padrão (+14 dias)")
    void createLoan_DefaultDueDate() {
        loanRequestDTO.setDueDate(null);
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        LoanResponseDTO result = loanService.createLoan(1L, loanRequestDTO);
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
    }

    // --- List Loans Tests ---

    @Test
    @DisplayName("Deve listar empréstimos do usuário")
    void getLoansByUserId_Success() {
        when(loanRepository.findByUserId(1L)).thenReturn(Collections.singletonList(loan));
        List<LoanResponseDTO> result = loanService.getLoansByUserId(1L);
        assertEquals(1, result.size());
    }

    // --- Return Loan Tests ---

    @ParameterizedTest
    @ValueSource(longs = { 50L, 100L, 1L })
    @DisplayName("Deve finalizar empréstimo com sucesso para diferentes IDs")
    void returnLoan_Parameterized(Long loanId) {
        Loan mockLoan = Loan.builder().id(loanId).status("ACTIVE").build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        LoanResponseDTO result = loanService.returnLoan(loanId);

        assertEquals("RETURNED", result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        verify(loanRepository).save(mockLoan);
    }
}