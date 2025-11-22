package com.librishare.backend.modules.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLibraryStatsDTO {
    private long totalBooks;
    private long booksRead;
    private long booksReading;
    private long booksToRead;
    private long activeLoans;
}