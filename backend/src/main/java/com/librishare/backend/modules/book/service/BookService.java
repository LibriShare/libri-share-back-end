package com.librishare.backend.modules.book.service;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    BookResponseDTO createBookInCatalog(BookRequestDTO requestDTO);

    BookResponseDTO findBookById(Long bookId);

    List<BookResponseDTO> findAllBooks();

    Book findOrCreateBook(BookRequestDTO requestDTO);

    Optional<BookResponseDTO> findByGoogleBooksId(String googleBooksId);

    BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO);
}