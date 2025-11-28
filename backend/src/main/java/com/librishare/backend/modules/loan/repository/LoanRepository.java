package com.librishare.backend.modules.loan.repository;

import com.librishare.backend.modules.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.userBook.user.id = :userId ORDER BY l.loanDate DESC")
    List<Loan> findByUserId(@Param("userId") Long userId);

    long countByUserBook_User_IdAndStatus(Long userId, String status);

    boolean existsByUserBookIdAndStatus(Long userBookId, String status);
}