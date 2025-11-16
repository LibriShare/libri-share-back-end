package com.librishare.backend.modules.book.repository;

import com.librishare.backend.modules.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByGoogleBooksId(String googleBooksId);

    Optional<Book> findByIsbn(String isbn);
}