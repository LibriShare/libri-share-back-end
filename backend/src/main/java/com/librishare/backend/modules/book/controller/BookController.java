package com.librishare.backend.modules.book.controller;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Catalogar (criar) um novo livro",
        description = "Cria um novo registro de livro na plataforma.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Livro catalogado com sucesso",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)) }),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (ex: erro de validação nos campos)", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado (usuário não autenticado)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO createdBook = bookService.createBook(requestDTO);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }
}