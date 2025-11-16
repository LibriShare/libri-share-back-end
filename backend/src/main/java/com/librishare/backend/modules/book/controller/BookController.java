package com.librishare.backend.modules.book.controller;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/books")
@Tag(name = "Catálogo de Livros", description = "Endpoints para gerenciar o catálogo global de livros")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "Adiciona um novo livro ao catálogo",
            description = "Cria uma nova entrada de livro no catálogo mestre. Usado quando um livro é registrado pela primeira vez no sistema (ex: via busca do Google Books)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: título ou autor faltando)",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Livro já existe no catálogo (ISBN ou Google ID duplicado)",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBookInCatalog(
            @Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO response = bookService.createBookInCatalog(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Lista todos os livros do catálogo",
            description = "Retorna uma lista paginada de todos os livros cadastrados no catálogo mestre da aplicação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de livros retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookResponseDTO.class))) // O Swagger entende que é uma lista
    })
    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        List<BookResponseDTO> books = bookService.findAllBooks();
        return ResponseEntity.ok(books);
    }

    @Operation(
            summary = "Busca um livro do catálogo por ID",
            description = "Retorna os detalhes de um livro específico do catálogo com base no seu ID único (PK do banco)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no catálogo com o ID fornecido",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.findBookById(id);
        return ResponseEntity.ok(book);
    }

    @Operation(
            summary = "Busca um livro do catálogo por Google Books ID",
            description = "Retorna os detalhes de um livro específico do catálogo com base no seu ID do Google Books (ex: 'P_6ADwAAQBAJ')."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no catálogo com o Google ID fornecido",
                    content = @Content)
    })
    @GetMapping("/google/{googleId}")
    public ResponseEntity<BookResponseDTO> getBookByGoogleId(@PathVariable String googleId) {
        BookResponseDTO book = bookService.findByGoogleBooksId(googleId)
                .orElseThrow(() -> new com.librishare.backend.exception.ResourceNotFoundException(
                        "Livro não encontrado com Google ID: " + googleId));
        return ResponseEntity.ok(book);
    }
}