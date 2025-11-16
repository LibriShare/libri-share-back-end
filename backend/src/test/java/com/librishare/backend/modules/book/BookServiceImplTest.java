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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        bookResponseDTO.setAuthor("Yuval Harari");
    }

    @Test
    @DisplayName("Deve criar um livro no catálogo com sucesso")
    void createBookInCatalog_Success() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.empty());
        when(mapper.map(bookRequestDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(mapper.map(book, BookResponseDTO.class)).thenReturn(bookResponseDTO);

        BookResponseDTO result = bookService.createBookInCatalog(bookRequestDTO);

        assertNotNull(result);
        assertEquals(bookResponseDTO.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).save(book);
    }
    
    @Test
    @DisplayName("Deve encontrar ou criar (criar novo)")
    void findOrCreateBook_CreatesNew() {
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(mapper.map(bookRequestDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.findOrCreateBook(bookRequestDTO);

        assertNotNull(result);
        verify(bookRepository, times(1)).save(book);
    }
}