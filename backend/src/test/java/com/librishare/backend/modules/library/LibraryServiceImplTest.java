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
        userBookResponse.setId(1L);
        userBookResponse.setBookId(1L);
        userBookResponse.setTitle("Test Book");
        userBookResponse.setAuthor("Test Author");
        userBookResponse.setStatus(ReadingStatus.WANT_TO_READ);
        userBookResponse.setTotalPages(100);
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
        verify(userBookRepository, times(1)).save(any(UserBook.class));
        verify(historyService, times(1)).logAction(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar livro duplicado na biblioteca")
    void addBookToLibrary_DuplicateBook_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(userBook));

        assertThrows(DuplicateResourceException.class,
                () -> libraryService.addBookToLibrary(1L, addBookRequest));

        verify(historyService, never()).logAction(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção se usuário não existir ao adicionar")
    void addBookToLibrary_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.addBookToLibrary(99L, addBookRequest));
    }

    // --- Get Library Tests ---

    @Test
    @DisplayName("Deve retornar lista de livros da estante")
    void getUserLibrary_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.singletonList(userBook));
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(userBookResponse);

        List<UserBookResponse> result = libraryService.getUserLibrary(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar listar estante de usuário inexistente")
    void getUserLibrary_UserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> libraryService.getUserLibrary(99L));
    }

    // --- Remove Book Tests ---

    @Test
    @DisplayName("Deve remover livro da biblioteca")
    void removeBookFromLibrary_Success() {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));

        libraryService.removeBookFromLibrary(1L, 1L);

        verify(userBookRepository, times(1)).delete(userBook);
    }

    // --- Update Status Tests ---

    @Test
    @DisplayName("Deve atualizar status para LENDO e definir data de início")
    void updateBookStatus_Reading() {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse responseMock = new UserBookResponse();
        responseMock.setStatus(ReadingStatus.READING);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(responseMock);

        UserBookResponse result = libraryService.updateBookStatus(1L, 1L, ReadingStatus.READING);

        assertNotNull(userBook.getStartedReadingAt());
        verify(userBookRepository).save(userBook);
    }

    // --- Update Progress Tests ---

    @Test
    @DisplayName("Deve atualizar progresso e mudar status para LIDO se completar páginas")
    void updateBookProgress_Finished() {
        book.setPages(100); // Livro tem 100 páginas

        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse responseMock = new UserBookResponse();
        responseMock.setCurrentPage(100);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(responseMock);

        libraryService.updateBookProgress(1L, 1L, 100);

        assertEquals(100, userBook.getCurrentPage());
        assertEquals(ReadingStatus.READ, userBook.getStatus());
        assertNotNull(userBook.getFinishedReadingAt());
    }

    @Test
    @DisplayName("Deve apenas atualizar progresso se não terminar")
    void updateBookProgress_NotFinished() {
        book.setPages(200);

        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBookResponse resp = new UserBookResponse();
        resp.setCurrentPage(50);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(resp);

        libraryService.updateBookProgress(1L, 1L, 50);

        assertEquals(50, userBook.getCurrentPage());
        // Status deve continuar o mesmo (WANT_TO_READ ou o que estava antes)
        assertEquals(ReadingStatus.WANT_TO_READ, userBook.getStatus());
    }

    // --- Update Rating Tests ---

    @Test
    @DisplayName("Deve atualizar avaliação (Rating)")
    void updateBookRating_Success() {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);
        when(mapper.map(any(UserBook.class), eq(UserBookResponse.class))).thenReturn(userBookResponse);

        libraryService.updateBookRating(1L, 1L, 5);

        assertEquals(5, userBook.getRating());
    }

    @Test
    @DisplayName("Deve lançar erro para nota inválida (<1 ou >5)")
    void updateBookRating_Invalid() {
        when(userBookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userBook));

        assertThrows(IllegalArgumentException.class, () ->
                libraryService.updateBookRating(1L, 1L, 6)
        );
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

        // Total Owned = READ + READING + TBR (5 + 2 + 3 = 10)
        assertEquals(10, stats.getTotalBooks());
        assertEquals(5, stats.getBooksRead());
        assertEquals(1, stats.getActiveLoans());
    }
}