package com.librishare.backend.modules.loan.service.impl;

import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.library.entity.UserBook;
import com.librishare.backend.modules.library.repository.UserBookRepository;
import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;
import com.librishare.backend.modules.loan.entity.Loan;
import com.librishare.backend.modules.loan.repository.LoanRepository;
import com.librishare.backend.modules.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserBookRepository userBookRepository;
    private final ModelMapper mapper;

    @Override
    public LoanResponseDTO createLoan(Long userId, LoanRequestDTO dto) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, dto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado na biblioteca do usuário."));

        Loan loan = Loan.builder()
                .userBook(userBook)
                .borrowerName(dto.getBorrowerName())
                .borrowerEmail(dto.getBorrowerEmail())
                .loanDate(LocalDate.now())
                .dueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().plusDays(14))
                .status("ACTIVE")
                .notes(dto.getNotes())
                .build();

        Loan savedLoan = loanRepository.save(loan);
        return mapToDTO(savedLoan);
    }

    @Override
    public List<LoanResponseDTO> getLoansByUserId(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public LoanResponseDTO returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado."));

        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDate.now());

        Loan savedLoan = loanRepository.save(loan);
        return mapToDTO(savedLoan);
    }

    private LoanResponseDTO mapToDTO(Loan loan) {
        LoanResponseDTO dto = mapper.map(loan, LoanResponseDTO.class);
        // Mapeamento manual de campos aninhados para facilitar pro front
        if (loan.getUserBook() != null && loan.getUserBook().getBook() != null) {
            dto.setBookId(loan.getUserBook().getBook().getId());
            dto.setBookTitle(loan.getUserBook().getBook().getTitle());
            dto.setBookCoverUrl(loan.getUserBook().getBook().getCoverImageUrl());
        }
        return dto;
    }
}