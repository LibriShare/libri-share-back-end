package com.librishare.backend.modules.library.service.impl;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.library.service.LibraryService;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    private UserBookRepository userBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public UserBookResponse addBookToLibrary(Long userId, AddBookRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com ID: " + request.getBookId()));

        userBookRepository.findByUserIdAndBookId(userId, request.getBookId()).ifPresent(ub -> {
            throw new DuplicateResourceException("Este livro já está na sua biblioteca.");
        });

        UserBook newUserBook = UserBook.builder()
                .user(user)
                .book(book)
                .status(request.getStatus())
                .addedAt(OffsetDateTime.now())
                .build();

        UserBook savedUserBook = userBookRepository.save(newUserBook);
        return mapToResponse(savedUserBook);
    }

    @Override
    public void removeBookFromLibrary(Long userId, Long userBookId) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrada da biblioteca não encontrada com ID: " + userBookId + " para este usuário."));
        userBookRepository.delete(userBook);
    }

    @Override
    public List<UserBookResponse> getUserLibrary(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
        }

        List<UserBook> library = userBookRepository.findByUserId(userId);
        return library.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserBookResponse updateBookStatus(Long userId, Long userBookId, ReadingStatus newStatus) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrada da biblioteca não encontrada com ID: " + userBookId + " para este usuário."));

        userBook.setStatus(newStatus);

        if (newStatus == ReadingStatus.READING && userBook.getStartedReadingAt() == null) {
            userBook.setStartedReadingAt(OffsetDateTime.now());
        } else if (newStatus == ReadingStatus.READ) {
            userBook.setFinishedReadingAt(OffsetDateTime.now());
        }

        UserBook updatedUserBook = userBookRepository.save(userBook);
        return mapToResponse(updatedUserBook);
    }

    // Helper para "achatar" a Entidade para o DTO
    private UserBookResponse mapToResponse(UserBook userBook) {
        UserBookResponse response = mapper.map(userBook, UserBookResponse.class);
        
        if (userBook.getBook() != null) {
            response.setBookId(userBook.getBook().getId());
            response.setTitle(userBook.getBook().getTitle());
            response.setAuthor(userBook.getBook().getAuthor());
            response.setCoverImageUrl(userBook.getBook().getCoverImageUrl());
        }
        
        return response;
    }
}