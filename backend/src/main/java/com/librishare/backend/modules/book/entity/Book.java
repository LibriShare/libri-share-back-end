package com.librishare.backend.modules.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "cover_image_url", length = 2048)
    private String coverImageUrl;

    @Column(name = "google_books_id", unique = true)
    private String googleBooksId;

    @Column(name = "synopsis", columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "purchase_url")
    private String purchaseUrl;
}