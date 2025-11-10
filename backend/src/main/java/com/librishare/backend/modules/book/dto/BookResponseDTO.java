package com.librishare.backend.modules.book.dto;

import lombok.Data;

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

}