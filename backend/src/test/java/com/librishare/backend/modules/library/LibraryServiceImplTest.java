package com.librishare.backend.modules.library;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.history.service.HistoryService;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.library.service.impl.LibraryServiceImpl;
import com.librishare.backend.modules.loan.repository.LoanRepository;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceImplTest {

    @Mock
    private UserBookRepository userBookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ModelMapper mapper;
    @Mock
    private HistoryService historyService;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    private User user;
    private Book book;
    private UserBook userBook;
    private AddBookRequest addBookRequest;
    private UserBookResponse userBookResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Test").build();
        book = Book.builder().id(1L).title("Test Book").author("Test Author").pages(100).build();

        addBookRequest = new AddBookRequest();
        addBookRequest.setBookId(1L);
        addBookRequest.setStatus(ReadingStatus.WANT_TO_READ);

        userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .status(ReadingStatus.WANT_TO_READ)
                .addedAt(OffsetDateTime.now())
                .build();

        userBookResponse = new UserBookResponse();
        userBookResponse.setTitle("Test Book");
    }

    // --- Add Book Tests ---

    @Test
    @DisplayName("Deve adicionar um livro à biblioteca com sucesso")
    void addBookToLibrary_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(userBookResponse);

        UserBookResponse result = libraryService.addBookToLibrary(1L, addBookRequest);

        assertNotNull(result);
        assertEquals(book.getTitle(), result.getTitle());
        verify(historyService).logAction(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar duplicado")
    void addBookToLibrary_Duplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(userBook));

        assertThrows(DuplicateResourceException.class, () -> libraryService.addBookToLibrary(1L, addBookRequest));
    }

    // --- Update Rating Tests ---

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Deve atualizar avaliação com notas válidas (1-5)")
    void updateBookRating_Parameterized(int rating) {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(userBookResponse);

        libraryService.updateBookRating(1L, 1L, rating);

        assertEquals(rating, userBook.getRating());
        verify(userBookRepository).save(userBook);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1})
    @DisplayName("Deve lançar erro para notas inválidas")
    void updateBookRating_Invalid(int invalidRating) {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));

        assertThrows(IllegalArgumentException.class, () ->
                libraryService.updateBookRating(1L, 1L, invalidRating)
        );
    }

    // --- Update Progress Tests ---

    @ParameterizedTest
    @CsvSource({
            "50, WANT_TO_READ",
            "99, WANT_TO_READ",
            "100, READ"
    })
    @DisplayName("Deve atualizar progresso e mudar status apenas se completar")
    void updateBookProgress_Parameterized(int pagesRead, ReadingStatus expectedStatus) {
        book.setPages(100);

        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(i -> i.getArgument(0));

        UserBookResponse resp = new UserBookResponse();
        resp.setCurrentPage(pagesRead);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(resp);

        libraryService.updateBookProgress(1L, 1L, pagesRead);

        assertEquals(pagesRead, userBook.getCurrentPage());
        assertEquals(expectedStatus, userBook.getStatus());
    }

    // --- Stats Tests ---

    @Test
    @DisplayName("Deve calcular estatísticas corretamente")
    void getUserLibraryStats_Success() {
        when(userBookRepository.countByUserIdAndStatus(1L, ReadingStatus.READ)).thenReturn(5L);
        when(userBookRepository.countByUserIdAndStatus(1L, ReadingStatus.READING)).thenReturn(2L);
        when(userBookRepository.countByUserIdAndStatus(1L, ReadingStatus.TO_READ)).thenReturn(3L);
        when(userBookRepository.countByUserIdAndStatus(1L, ReadingStatus.WANT_TO_READ)).thenReturn(10L);
        when(loanRepository.countByUserBook_User_IdAndStatus(1L, "ACTIVE")).thenReturn(1L);

        UserLibraryStatsDTO stats = libraryService.getUserLibraryStats(1L);

        assertEquals(10, stats.getTotalBooks()); // 5+2+3
        assertEquals(5, stats.getBooksRead());
        assertEquals(1, stats.getActiveLoans());
    }
}