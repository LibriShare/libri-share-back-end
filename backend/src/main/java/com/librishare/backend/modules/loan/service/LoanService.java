package com.librishare.backend.modules.loan.service;

import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;

import java.util.List;

public interface LoanService {
    LoanResponseDTO createLoan(Long userId, LoanRequestDTO loanRequestDTO);
    List<LoanResponseDTO> getLoansByUserId(Long userId);
    LoanResponseDTO returnLoan(Long loanId);
}