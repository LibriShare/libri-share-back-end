package com.librishare.backend.modules.library.dto;

import com.librishare.backend.modules.library.enums.ReadingStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserBookResponse {

    private Long id;
    private ReadingStatus status;
    private Integer rating;
    private String review;
    private OffsetDateTime addedAt;

    private Integer currentPage;

    // Dados do Livro
    private Long bookId;
    private String title;
    private String author;
    private String coverImageUrl;
    private Integer totalPages;
}