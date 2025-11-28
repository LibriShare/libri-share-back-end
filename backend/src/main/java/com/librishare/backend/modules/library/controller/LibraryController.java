package com.librishare.backend.modules.library.controller;

import com.librishare.backend.modules.library.dto.AddBookRequest;
import com.librishare.backend.modules.library.dto.UserBookResponse;
import com.librishare.backend.modules.library.dto.UserLibraryStatsDTO;
import com.librishare.backend.modules.library.enums.ReadingStatus;
import com.librishare.backend.modules.library.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/{userId}/library")
@Tag(name = "Biblioteca Pessoal", description = "Endpoints para gerenciar a estante de livros de um usuário específico")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(
            summary = "Adiciona um livro à estante do usuário",
            description = "Adiciona um livro (que já existe no catálogo global) à biblioteca pessoal de um usuário, definindo um status inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro adicionado à estante com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou status nulo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário ou Livro não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Livro já existe na biblioteca deste usuário", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserBookResponse> addBookToLibrary(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable Long userId,
            @Valid @RequestBody AddBookRequest request) {
        UserBookResponse response = libraryService.addBookToLibrary(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Busca a estante completa de um usuário",
            description = "Retorna uma lista de todos os livros que o usuário possui em sua estante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estante retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserBookResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserBookResponse>> getUserLibrary(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable Long userId) {
        List<UserBookResponse> library = libraryService.getUserLibrary(userId);
        return ResponseEntity.ok(library);
    }

    @Operation(
            summary = "Remove um livro da estante",
            description = "Remove uma entrada específica (UserBook) da biblioteca do usuário. Não remove do catálogo global."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livro removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Entrada não encontrada ou não pertence ao usuário", content = @Content)
    })
    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> removeBookFromLibrary(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable Long userId,
            @Parameter(description = "ID da relação usuário-livro (UserBook)", example = "10") @PathVariable Long userBookId) {
        libraryService.removeBookFromLibrary(userId, userBookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualiza o status de leitura",
            description = "Muda o status de um livro (ex: de 'WANT_TO_READ' para 'READING').",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novo status", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\": \"READING\"}")))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    @PatchMapping("/{userBookId}/status")
    public ResponseEntity<UserBookResponse> updateBookStatus(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do UserBook") @PathVariable Long userBookId,
            @RequestBody Map<String, String> statusUpdate) {

        ReadingStatus newStatus;
        try {
            String status = statusUpdate.get("status");
            if (status == null) throw new IllegalArgumentException("O campo 'status' é obrigatório.");
            newStatus = ReadingStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Status inválido. Use WANT_TO_READ, READING, ou READ.");
        }
        UserBookResponse response = libraryService.updateBookStatus(userId, userBookId, newStatus);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Atualiza o progresso de leitura",
            description = "Define em qual página o usuário está.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Número da página atual", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"currentPage\": 50}")))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Página inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    @PatchMapping("/{userBookId}/progress")
    public ResponseEntity<UserBookResponse> updateBookProgress(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do UserBook") @PathVariable Long userBookId,
            @RequestBody Map<String, Integer> progressUpdate) {

        Integer currentPage = progressUpdate.get("currentPage");
        if (currentPage == null || currentPage < 0) throw new IllegalArgumentException("Página atual inválida.");

        UserBookResponse response = libraryService.updateBookProgress(userId, userBookId, currentPage);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Avalia um livro (1-5 estrelas)",
            description = "Define ou atualiza a nota dada pelo usuário ao livro.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nota de 1 a 5", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"rating\": 5}")))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nota inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    @PatchMapping("/{userBookId}/rating")
    public ResponseEntity<UserBookResponse> updateBookRating(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do UserBook") @PathVariable Long userBookId,
            @RequestBody Map<String, Integer> ratingUpdate) {

        Integer rating = ratingUpdate.get("rating");
        if (rating == null) throw new IllegalArgumentException("O campo 'rating' é obrigatório.");

        UserBookResponse response = libraryService.updateBookRating(userId, userBookId, rating);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Adiciona ou atualiza a resenha",
            description = "Salva um comentário de texto sobre o livro.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Texto da resenha", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"review\": \"Livro excelente!\"}")))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resenha atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Texto inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    @PatchMapping("/{userBookId}/review")
    public ResponseEntity<UserBookResponse> updateBookReview(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do UserBook") @PathVariable Long userBookId,
            @RequestBody Map<String, String> reviewUpdate) {

        String review = reviewUpdate.get("review");
        if (review == null) throw new IllegalArgumentException("O campo 'review' é obrigatório.");

        UserBookResponse response = libraryService.updateBookReview(userId, userBookId, review);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtém estatísticas de leitura do usuário",
            description = "Retorna contagens de livros lidos, páginas lidas, livros por status, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserLibraryStatsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/stats")
    public ResponseEntity<UserLibraryStatsDTO> getLibraryStats(
            @Parameter(description = "ID do usuário") @PathVariable Long userId) {
        return ResponseEntity.ok(libraryService.getUserLibraryStats(userId));
    }
}