package com.librishare.backend.modules.book.service.impl;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public BookResponseDTO createBookInCatalog(BookRequestDTO requestDTO) {
        validateBookUniqueness(requestDTO.getIsbn(), requestDTO.getGoogleBooksId());

        Book book = modelMapper.map(requestDTO, Book.class);
        Book savedBook = bookRepository.save(book);
        return modelMapper.map(savedBook, BookResponseDTO.class);
    }

    @Override
    public BookResponseDTO findBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado no catálogo com ID: " + bookId));
        return modelMapper.map(book, BookResponseDTO.class);
    }

    @Override
    public List<BookResponseDTO> findAllBooks() {
        List<Book> books = bookRepository.findAll();
        Type listType = new TypeToken<List<BookResponseDTO>>() {}.getType();
        return modelMapper.map(books, listType);
    }

    @Override
    public Optional<BookResponseDTO> findByGoogleBooksId(String googleBooksId) {
        return bookRepository.findByGoogleBooksId(googleBooksId)
                .map(book -> modelMapper.map(book, BookResponseDTO.class));
    }

    @Override
    public Book findOrCreateBook(BookRequestDTO requestDTO) {
        // 1. Tenta encontrar pelo Google Books ID
        if (requestDTO.getGoogleBooksId() != null && !requestDTO.getGoogleBooksId().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByGoogleBooksId(requestDTO.getGoogleBooksId());
            if (existingBook.isPresent()) {
                return existingBook.get();
            }
        }

        // 2. Tenta encontrar pelo ISBN
        if (requestDTO.getIsbn() != null && !requestDTO.getIsbn().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(requestDTO.getIsbn());
            if (existingBook.isPresent()) {
                return existingBook.get();
            }
        }

        // 3. Se não encontrou, cria um novo livro no catálogo
        Book newBook = modelMapper.map(requestDTO, Book.class);
        return bookRepository.save(newBook);
    }

    private void validateBookUniqueness(String isbn, String googleBooksId) {
        if (isbn != null) {
            bookRepository.findByIsbn(isbn).ifPresent(b -> {
                throw new DuplicateResourceException("Livro já cadastrado no catálogo com este ISBN: " + isbn);
            });
        }
        if (googleBooksId != null) {
            bookRepository.findByGoogleBooksId(googleBooksId).ifPresent(b -> {
                throw new DuplicateResourceException("Livro já cadastrado no catálogo com este Google Books ID: " + googleBooksId);
            });
        }
    }
}