package com.librishare.backend.modules.loan.controller;

import com.librishare.backend.modules.loan.dto.LoanRequestDTO;
import com.librishare.backend.modules.loan.dto.LoanResponseDTO;
import com.librishare.backend.modules.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/loans")
@Tag(name = "Gestão de Empréstimos", description = "Endpoints para criar e finalizar empréstimos de livros a terceiros")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Registra um novo empréstimo",
            description = "Cria um registro de que um livro da biblioteca do usuário foi emprestado para uma terceira pessoa. O status do livro na estante muda para 'LOANED'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empréstimo registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: data de devolução anterior à data atual)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado na biblioteca do usuário",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(
            @Parameter(description = "ID do usuário que está emprestando o livro", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody LoanRequestDTO loanRequestDTO) {
        LoanResponseDTO newLoan = loanService.createLoan(userId, loanRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLoan);
    }

    @Operation(
            summary = "Histórico de empréstimos do usuário",
            description = "Retorna uma lista com todos os empréstimos (ativos e finalizados) realizados pelo usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getUserLoans(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId) {
        List<LoanResponseDTO> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Registra a devolução do livro",
            description = "Marca um empréstimo ativo como 'devolvido' (RETURNED), define a data de devolução real e libera o livro na estante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolução registrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content)
    })
    @PatchMapping("/{loanId}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @Parameter(description = "ID do usuário (dono do livro)", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID do empréstimo a ser finalizado", example = "50")
            @PathVariable Long loanId) {

        LoanResponseDTO returnedLoan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(returnedLoan);
    }
}