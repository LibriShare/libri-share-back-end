package com.librishare.backend.modules.book.service.impl;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.book.service.BookService;
import jakarta.transaction.Transactional;
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
        if (requestDTO.getGoogleBooksId() != null && !requestDTO.getGoogleBooksId().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByGoogleBooksId(requestDTO.getGoogleBooksId());
            if (existingBook.isPresent()) {
                return existingBook.get();
            }
        }

        if (requestDTO.getIsbn() != null && !requestDTO.getIsbn().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(requestDTO.getIsbn());
            if (existingBook.isPresent()) {
                return existingBook.get();
            }
        }

        Book newBook = modelMapper.map(requestDTO, Book.class);
        return bookRepository.save(newBook);
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com ID: " + id));

        if (requestDTO.getSynopsis() != null) {
            book.setSynopsis(requestDTO.getSynopsis());
        }

        if (requestDTO.getCoverImageUrl() != null && !requestDTO.getCoverImageUrl().isEmpty()) {
            book.setCoverImageUrl(requestDTO.getCoverImageUrl());
        }

        if (requestDTO.getTitle() != null && !requestDTO.getTitle().isEmpty()) {
            book.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getAuthor() != null && !requestDTO.getAuthor().isEmpty()) {
            book.setAuthor(requestDTO.getAuthor());
        }

        if (requestDTO.getPrice() != null) {
            book.setPrice(requestDTO.getPrice());
        }
        if (requestDTO.getPurchaseUrl() != null) {
            book.setPurchaseUrl(requestDTO.getPurchaseUrl());
        }

        Book updatedBook = bookRepository.save(book);
        return modelMapper.map(updatedBook, BookResponseDTO.class);

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