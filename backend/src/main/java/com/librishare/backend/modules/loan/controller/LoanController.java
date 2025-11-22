package com.librishare.backend.modules.loan.controller;

import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;
import com.librishare.backend.modules.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/loans")
@Tag(name = "Empréstimos", description = "Endpoints para gerenciamento de empréstimos de livros")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Operation(
            summary = "Cria um novo empréstimo",
            description = "Registra o empréstimo de um livro da biblioteca do usuário para uma terceira pessoa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empréstimo criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado na biblioteca do usuário",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(
            @PathVariable Long userId,
            @Valid @RequestBody LoanRequestDTO loanRequestDTO) {
        LoanResponseDTO newLoan = loanService.createLoan(userId, loanRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLoan);
    }

    @Operation(
            summary = "Lista empréstimos do usuário",
            description = "Retorna o histórico de todos os empréstimos realizados pelo usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de empréstimos retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getUserLoans(@PathVariable Long userId) {
        List<LoanResponseDTO> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Devolver livro",
            description = "Marca um empréstimo como devolvido e registra a data de devolução."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empréstimo atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content)
    })
    @PatchMapping("/{loanId}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long userId,
            @PathVariable Long loanId) {
        LoanResponseDTO returnedLoan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(returnedLoan);
    }
}