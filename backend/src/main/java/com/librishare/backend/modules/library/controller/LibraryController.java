package com.librishare.backend.modules.library.controller;

import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/{userId}/library")
@Tag(name = "Biblioteca Pessoal", description = "Endpoints para gerenciar a estante de livros de um usuário específico")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @Operation(
            summary = "Adiciona um livro à estante do usuário",
            description = "Adiciona um livro (que já existe no catálogo global) à biblioteca pessoal de um usuário, definindo um status inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro adicionado à estante com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: bookId ou status nulos)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário ou Livro não encontrado no sistema",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Este livro já está na biblioteca deste usuário",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserBookResponse> addBookToLibrary(
            @PathVariable Long userId,
            @Valid @RequestBody AddBookRequest request) {
        UserBookResponse response = libraryService.addBookToLibrary(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Busca a estante completa de um usuário",
            description = "Retorna uma lista de todos os livros (UserBookResponse) que um usuário possui em sua estante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estante retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserBookResponse.class))), // Swagger entende que é uma lista
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserBookResponse>> getUserLibrary(@PathVariable Long userId) {
        List<UserBookResponse> library = libraryService.getUserLibrary(userId);
        return ResponseEntity.ok(library);
    }

    @Operation(
            summary = "Remove um livro da estante do usuário",
            description = "Remove uma entrada específica (UserBook) da biblioteca do usuário. Isso não remove o livro do catálogo global."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livro removido da estante com sucesso",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Entrada da biblioteca não encontrada (ou não pertence ao usuário)",
                    content = @Content)
    })
    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> removeBookFromLibrary(
            @PathVariable Long userId,
            @PathVariable Long userBookId) {
        libraryService.removeBookFromLibrary(userId, userBookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualiza o status de leitura de um livro",
            description = "Muda o status de um livro na estante do usuário (ex: de 'WANT_TO_READ' para 'READING').",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "O novo status desejado para o livro.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"status\": \"READING\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido (deve ser WANT_TO_READ, READING ou READ)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Entrada da biblioteca não encontrada (ou não pertence ao usuário)",
                    content = @Content)
    })
    @PatchMapping("/{userBookId}/status")
    public ResponseEntity<UserBookResponse> updateBookStatus(
            @PathVariable Long userId,
            @PathVariable Long userBookId,
            @RequestBody Map<String, String> statusUpdate) {

        ReadingStatus newStatus;
        try {
            String status = statusUpdate.get("status");
            if (status == null) {
                throw new IllegalArgumentException("O campo 'status' é obrigatório.");
            }
            newStatus = ReadingStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Status inválido. Use WANT_TO_READ, READING, ou READ.");
        }

        UserBookResponse response = libraryService.updateBookStatus(userId, userBookId, newStatus);
        return ResponseEntity.ok(response);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Página atual inválida (nula ou negativa)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Entrada da biblioteca não encontrada",
                    content = @Content)
    })
    @PatchMapping("/{userBookId}/progress")
    public ResponseEntity<UserBookResponse> updateBookProgress(
            @PathVariable Long userId,
            @PathVariable Long userBookId,
            @RequestBody Map<String, Integer> progressUpdate) {

        Integer currentPage = progressUpdate.get("currentPage");
        if (currentPage == null || currentPage < 0) {
            throw new IllegalArgumentException("Página atual inválida.");
        }

        UserBookResponse response = libraryService.updateBookProgress(userId, userBookId, currentPage);
        return ResponseEntity.ok(response);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Página atual inválida (nula ou negativa)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Entrada da biblioteca não encontrada",
                    content = @Content)
    })
    @Operation(summary = "Avalia um livro (1-5 estrelas)")
    @PatchMapping("/{userBookId}/rating")
    public ResponseEntity<UserBookResponse> updateBookRating(
            @PathVariable Long userId,
            @PathVariable Long userBookId,
            @RequestBody Map<String, Integer> ratingUpdate) {

        Integer rating = ratingUpdate.get("rating");
        if (rating == null) {
            throw new IllegalArgumentException("O campo 'rating' é obrigatório.");
        }

        UserBookResponse response = libraryService.updateBookRating(userId, userBookId, rating);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Adiciona ou atualiza a resenha de um livro")
    @PatchMapping("/{userBookId}/review")
    public ResponseEntity<UserBookResponse> updateBookReview(
            @PathVariable Long userId,
            @PathVariable Long userBookId,
            @RequestBody Map<String, String> reviewUpdate) {

        String review = reviewUpdate.get("review");
        if (review == null) {
            throw new IllegalArgumentException("O campo 'review' é obrigatório.");
        }

        UserBookResponse response = libraryService.updateBookReview(userId, userBookId, review);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<UserLibraryStatsDTO> getLibraryStats(@PathVariable Long userId) {
        return ResponseEntity.ok(libraryService.getUserLibraryStats(userId));
    }
}