package com.librishare.backend.modules.library;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.history.service.HistoryService;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.library.service.impl.LibraryServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
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
    private ModelMapper mapper;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Mock
    private HistoryService historyService;

    private User user;
    private Book book;
    private UserBook userBook;
    private AddBookRequest addBookRequest;
    private UserBookResponse userBookResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Test").build();
        book = Book.builder().id(1L).title("Test Book").author("Test Author").build();

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
    }

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
        assertEquals(ReadingStatus.WANT_TO_READ, result.getStatus());
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar livro duplicado na biblioteca")
    void addBookToLibrary_DuplicateBook_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(userBook));

        assertThrows(DuplicateResourceException.class,
                () -> libraryService.addBookToLibrary(1L, addBookRequest));
    }
}