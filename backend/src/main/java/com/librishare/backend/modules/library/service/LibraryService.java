package com.librishare.backend.modules.library.service;

import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.enums.ReadingStatus;

import java.util.List;

public interface LibraryService {

    UserBookResponse addBookToLibrary(Long userId, AddBookRequest request);

    void removeBookFromLibrary(Long userId, Long userBookId);

    List<UserBookResponse> getUserLibrary(Long userId);

    UserBookResponse updateBookStatus(Long userId, Long userBookId, ReadingStatus newStatus);

    UserBookResponse updateBookProgress(Long userId, Long userBookId, Integer currentPage);

    UserBookResponse updateBookRating(Long userId, Long userBookId, Integer rating);

    UserBookResponse updateBookReview(Long userId, Long userBookId, String review);

    UserLibraryStatsDTO getUserLibraryStats(Long userId);
}