package com.librishare.backend.modules.library.entity;

import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "user_books",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReadingStatus status;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "added_at", updatable = false)
    @Builder.Default
    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Column(name = "started_reading_at")
    private OffsetDateTime startedReadingAt;

    @Column(name = "finished_reading_at")
    private OffsetDateTime finishedReadingAt;

    @Column(name = "current_page")
    private Integer currentPage = 0;
}