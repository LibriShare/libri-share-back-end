package com.librishare.backend.modules.book.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private Integer pages;
    private String coverImageUrl;
    private String googleBooksId;
    private String synopsis;
    private BigDecimal price;
    private String purchaseUrl;
}