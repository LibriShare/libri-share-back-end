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
        book = Book.builder().id(10L).title("Dom Quixote").author("Miguel de Cervantes").build();

        userBook = UserBook.builder()
                .id(100L)
                .user(user)
                .book(book)
                .status(ReadingStatus.READ)
                .build();

        loanRequestDTO = new LoanRequestDTO();
        loanRequestDTO.setBookId(10L);
        loanRequestDTO.setBorrowerName("João da Silva");
        loanRequestDTO.setBorrowerEmail("joao@email.com");
        loanRequestDTO.setNotes("Cuidar bem");

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

    @Test
    @DisplayName("Deve criar um empréstimo com sucesso (Status READ)")
    void createLoan_Success() {
        // Setup
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(loanRepository.existsByUserBookIdAndStatus(100L, "ACTIVE")).thenReturn(false); // Não está emprestado
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO result = loanService.createLoan(1L, loanRequestDTO);

        assertNotNull(result);
        assertEquals("João da Silva", result.getBorrowerName());
        assertEquals("Dom Quixote", result.getBookTitle());

        verify(historyService, times(1)).logAction(eq(user), eq("EMPRÉSTIMO"), anyString());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    @DisplayName("Deve lançar erro se o livro não estiver na biblioteca do usuário")
    void createLoan_BookNotFound() {
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            loanService.createLoan(1L, loanRequestDTO)
        );
        
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro se status do livro for inválido (ex: LENDO)")
    void createLoan_InvalidStatus() {
        userBook.setStatus(ReadingStatus.READING); // Status inválido para empréstimo
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            loanService.createLoan(1L, loanRequestDTO)
        );

        assertTrue(ex.getMessage().contains("só pode emprestar livros que já leu"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro se o livro já estiver emprestado (Ativo)")
    void createLoan_AlreadyLent() {
        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(loanRepository.existsByUserBookIdAndStatus(100L, "ACTIVE")).thenReturn(true); // Já existe

        assertThrows(DuplicateResourceException.class, () -> 
            loanService.createLoan(1L, loanRequestDTO)
        );

        verify(loanRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve definir data de devolução padrão (+14 dias) se não informada")
    void createLoan_DefaultDueDate() {
        loanRequestDTO.setDueDate(null);

        when(userBookRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(userBook));
        when(loanRepository.existsByUserBookIdAndStatus(100L, "ACTIVE")).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            return saved;
        });
        
        LoanResponseDTO result = loanService.createLoan(1L, loanRequestDTO);

        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
    }

    // --- List Loans Tests ---

    @Test
    @DisplayName("Deve listar empréstimos do usuário")
    void getLoansByUserId_Success() {
        when(loanRepository.findByUserId(1L)).thenReturn(Collections.singletonList(loan));

        List<LoanResponseDTO> result = loanService.getLoansByUserId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("João da Silva", result.get(0).getBorrowerName());
    }

    // --- Return Loan Tests ---

    @Test
    @DisplayName("Deve finalizar empréstimo com sucesso (Devolução)")
    void returnLoan_Success() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto

        LoanResponseDTO result = loanService.returnLoan(50L);

        assertEquals("RETURNED", result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar devolver empréstimo inexistente")
    void returnLoan_NotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.returnLoan(99L));
        
        verify(loanRepository, never()).save(any());
    }
}