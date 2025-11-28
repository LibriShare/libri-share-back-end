package com.librishare.backend.modules.book;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.book.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;

    @BeforeEach
    void setUp() {
        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Sapiens");
        bookRequestDTO.setAuthor("Yuval Harari");
        bookRequestDTO.setIsbn("1234567890123");
        bookRequestDTO.setGoogleBooksId("googleId123");

        book = Book.builder()
                .id(1L)
                .title("Sapiens")
                .author("Yuval Harari")
                .isbn("1234567890123")
                .googleBooksId("googleId123")
                .build();

        bookResponseDTO = new BookResponseDTO();
        bookResponseDTO.setId(1L);
        bookResponseDTO.setTitle("Sapiens");
    }

    // --- Create Tests ---

    @ParameterizedTest
    @CsvSource({
            "Sapiens, Yuval Harari, 1234567890123, google1",
            "Clean Code, Uncle Bob, 9876543210987, google2",
            "1984, George Orwell, 1112223334445, google3"
    })
    @DisplayName("Deve criar livros no catálogo com diferentes dados")
    void createBookInCatalog_Parameterized(String title, String author, String isbn, String googleId) {
        // Arrange
        BookRequestDTO req = new BookRequestDTO();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        req.setGoogleBooksId(googleId);

        Book mappedBook = Book.builder().title(title).author(author).build();
        BookResponseDTO response = new BookResponseDTO();
        response.setTitle(title);

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(bookRepository.findByGoogleBooksId(googleId)).thenReturn(Optional.empty());
        when(mapper.map(req, Book.class)).thenReturn(mappedBook);
        when(bookRepository.save(any(Book.class))).thenReturn(mappedBook);
        when(mapper.map(mappedBook, BookResponseDTO.class)).thenReturn(response);

        // Act
        BookResponseDTO result = bookService.createBookInCatalog(req);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        verify(bookRepository).save(mappedBook);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar livro com ISBN duplicado")
    void createBookInCatalog_ThrowsDuplicateIsbn() {
        when(bookRepository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.of(book));
        assertThrows(DuplicateResourceException.class, () -> bookService.createBookInCatalog(bookRequestDTO));
    }

    // --- Find Tests ---

    @Test
    @DisplayName("Deve retornar um livro por ID")
    void findBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(mapper.map(book, BookResponseDTO.class)).thenReturn(bookResponseDTO);
        assertNotNull(bookService.findBookById(1L));
    }

    @Test
    @DisplayName("Deve retornar todos os livros")
    void findAllBooks_Success() {
        when(bookRepository.findAll()).thenReturn(Collections.singletonList(book));
        when(mapper.map(any(), any(Type.class))).thenReturn(Collections.singletonList(bookResponseDTO));
        assertFalse(bookService.findAllBooks().isEmpty());
    }

    // --- Find Or Create Logic ---

    @Test
    @DisplayName("FindOrCreate: Retorna existente se encontrado por Google ID")
    void findOrCreateBook_ExistingGoogleId() {
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.of(book));
        Book result = bookService.findOrCreateBook(bookRequestDTO);
        assertEquals(book.getId(), result.getId());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("FindOrCreate: Cria novo se não existir")
    void findOrCreateBook_New() {
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(mapper.map(any(), eq(Book.class))).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.findOrCreateBook(bookRequestDTO);
        assertNotNull(result);
        verify(bookRepository).save(book);
    }

    // --- Update Tests ---

    @ParameterizedTest
    @CsvSource({
            "Novo Titulo, Novo Autor",
            "Edição Especial, Autor Famoso"
    })
    @DisplayName("Deve atualizar livro com diferentes dados")
    void updateBook_Parameterized(String newTitle, String newAuthor) {
        BookRequestDTO updateReq = new BookRequestDTO();
        updateReq.setTitle(newTitle);
        updateReq.setAuthor(newAuthor);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO response = new BookResponseDTO();
        response.setTitle(newTitle);
        when(mapper.map(any(Book.class), eq(BookResponseDTO.class))).thenReturn(response);

        BookResponseDTO result = bookService.updateBook(1L, updateReq);

        assertEquals(newTitle, result.getTitle());
        verify(bookRepository).save(book);
    }
}