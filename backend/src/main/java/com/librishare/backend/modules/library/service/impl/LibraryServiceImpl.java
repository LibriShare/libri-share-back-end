package com.librishare.backend.modules.library.service.impl;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.history.service.HistoryService;
import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.library.service.LibraryService;
import com.librishare.backend.modules.loan.repository.LoanRepository;
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
    private LoanRepository loanRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private HistoryService historyService;

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

        // --- LOG DE HISTÓRICO INTELIGENTE ---
        String actionType = "BIBLIOTECA";
        String description = "Adicionou '" + book.getTitle() + "' à estante.";

        if (request.getStatus() == ReadingStatus.WANT_TO_READ) {
            actionType = "LISTA DE DESEJOS";
            description = "Adicionou '" + book.getTitle() + "' à lista de desejos.";
        } else if (request.getStatus() == ReadingStatus.READING) {
            actionType = "LEITURA";
            description = "Começou a ler '" + book.getTitle() + "'.";
        }

        historyService.logAction(user, actionType, description);

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

    @Override
    public UserBookResponse updateBookProgress(Long userId, Long userBookId, Integer currentPage) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrada da biblioteca não encontrada com ID: " + userBookId));

        userBook.setCurrentPage(currentPage);

        if (userBook.getBook().getPages() != null && currentPage >= userBook.getBook().getPages()) {
            userBook.setStatus(ReadingStatus.READ);
            userBook.setFinishedReadingAt(OffsetDateTime.now());
        }

        UserBook updated = userBookRepository.save(userBook);
        return mapToResponse(updated);
    }

    @Override
    public UserBookResponse updateBookRating(Long userId, Long userBookId, Integer rating) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrada da biblioteca não encontrada com ID: " + userBookId));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("A nota deve ser entre 1 e 5.");
        }

        userBook.setRating(rating);
        UserBook updated = userBookRepository.save(userBook);
        return mapToResponse(updated);
    }

    @Override
    public UserBookResponse updateBookReview(Long userId, Long userBookId, String review) {
        UserBook userBook = userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada não encontrada."));

        userBook.setReview(review);
        UserBook updated = userBookRepository.save(userBook);
        return mapToResponse(updated);
    }

    @Override
    public UserLibraryStatsDTO getUserLibraryStats(Long userId) {
        long read = userBookRepository.countByUserIdAndStatus(userId, ReadingStatus.READ);
        long reading = userBookRepository.countByUserIdAndStatus(userId, ReadingStatus.READING);
        long tbr = userBookRepository.countByUserIdAndStatus(userId, ReadingStatus.TO_READ); // <--- CONTA TBR
        long wishlist = userBookRepository.countByUserIdAndStatus(userId, ReadingStatus.WANT_TO_READ);

        // Total da Biblioteca = Lidos + Lendo + Para Ler
        long totalOwned = read + reading + tbr;
        long activeLoans = loanRepository.countByUserBook_User_IdAndStatus(userId, "ACTIVE");

        return new UserLibraryStatsDTO(totalOwned, read, reading, wishlist, activeLoans);
    }

    private UserBookResponse mapToResponse(UserBook userBook) {
        UserBookResponse response = mapper.map(userBook, UserBookResponse.class);

        response.setCurrentPage(userBook.getCurrentPage() != null ? userBook.getCurrentPage() : 0);

        if (userBook.getBook() != null) {
            response.setBookId(userBook.getBook().getId());
            response.setTitle(userBook.getBook().getTitle());
            response.setAuthor(userBook.getBook().getAuthor());
            response.setCoverImageUrl(userBook.getBook().getCoverImageUrl());
            response.setTotalPages(userBook.getBook().getPages());
        }

        return response;
    }
}