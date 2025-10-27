package com.librishare.backend.modules.book.dto;

import com.librishare.backend.modules.book.enums.BookStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookResponseDTO {
    
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private Integer totalPages;
    private Integer publicationYear;
    private String publisher;
    private String coverUrl;
    private BookStatus status;
    private Integer pagesRead;
    private Integer rating;
    private UUID userId;
    private OffsetDateTime createdAt;
}