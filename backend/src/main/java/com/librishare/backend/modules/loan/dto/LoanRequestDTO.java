package com.librishare.backend.modules.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequestDTO {

    @NotNull(message = "O ID do livro na biblioteca é obrigatório")
    private Long bookId;

    @NotBlank(message = "O nome da pessoa que pegou emprestado é obrigatório")
    private String borrowerName;

    private String borrowerEmail;
    private LocalDate dueDate;
    private String notes;
}