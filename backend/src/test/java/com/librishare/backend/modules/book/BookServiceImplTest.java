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
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
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

    // --- Create Tests ---

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
    @DisplayName("Deve lançar exceção ao tentar criar livro com ISBN duplicado")
    void createBookInCatalog_ThrowsDuplicateIsbn() {
        when(bookRepository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.of(book));

        assertThrows(DuplicateResourceException.class, () ->
                bookService.createBookInCatalog(bookRequestDTO)
        );

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar livro com Google ID duplicado")
    void createBookInCatalog_ThrowsDuplicateGoogleId() {
        // ISBN não existe, mas Google ID sim
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByGoogleBooksId(bookRequestDTO.getGoogleBooksId())).thenReturn(Optional.of(book));

        assertThrows(DuplicateResourceException.class, () ->
                bookService.createBookInCatalog(bookRequestDTO)
        );

        verify(bookRepository, never()).save(any());
    }

    // --- Find Tests ---

    @Test
    @DisplayName("Deve retornar um livro por ID com sucesso")
    void findBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(mapper.map(book, BookResponseDTO.class)).thenReturn(bookResponseDTO);

        BookResponseDTO result = bookService.findBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando livro não encontrado por ID")
    void findBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.findBookById(1L));
    }

    @Test
    @DisplayName("Deve retornar todos os livros")
    void findAllBooks_Success() {
        List<Book> books = Collections.singletonList(book);
        List<BookResponseDTO> responses = Collections.singletonList(bookResponseDTO);

        when(bookRepository.findAll()).thenReturn(books);
        // Atenção aqui: Como é uma lista genérica, usamos any(Type.class) ou matchers mais complexos.
        // O mais simples é aceitar qualquer Type no mock do mapper para listas.
        when(mapper.map(eq(books), any(Type.class))).thenReturn(responses);

        List<BookResponseDTO> result = bookService.findAllBooks();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // --- Find Or Create Logic ---

    @Test
    @DisplayName("FindOrCreate: Deve retornar livro existente pelo Google ID (sem salvar novo)")
    void findOrCreateBook_ReturnsExistingByGoogleId() {
        when(bookRepository.findByGoogleBooksId(bookRequestDTO.getGoogleBooksId()))
                .thenReturn(Optional.of(book));

        Book result = bookService.findOrCreateBook(bookRequestDTO);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        // Garante que NÃO chamou o save
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("FindOrCreate: Deve retornar livro existente pelo ISBN (sem salvar novo)")
    void findOrCreateBook_ReturnsExistingByIsbn() {
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.of(book));

        Book result = bookService.findOrCreateBook(bookRequestDTO);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("FindOrCreate: Deve criar novo quando não existe")
    void findOrCreateBook_CreatesNew() {
        when(bookRepository.findByGoogleBooksId(anyString())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(mapper.map(bookRequestDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.findOrCreateBook(bookRequestDTO);

        assertNotNull(result);
        verify(bookRepository, times(1)).save(book);
    }

    // --- Update Tests ---

    @Test
    @DisplayName("Deve atualizar um livro com sucesso")
    void updateBook_Success() {
        BookRequestDTO updateRequest = new BookRequestDTO();
        updateRequest.setTitle("Sapiens: Atualizado");

        // Comportamento: acha o livro antigo
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        // Salva e retorna o livro (o objeto book foi modificado na service)
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO updatedResponse = new BookResponseDTO();
        updatedResponse.setTitle("Sapiens: Atualizado");
        when(mapper.map(any(Book.class), eq(BookResponseDTO.class))).thenReturn(updatedResponse);

        BookResponseDTO result = bookService.updateBook(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Sapiens: Atualizado", result.getTitle());
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar atualizar livro inexistente")
    void updateBook_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookService.updateBook(1L, bookRequestDTO)
        );

        verify(bookRepository, never()).save(any());
    }
}