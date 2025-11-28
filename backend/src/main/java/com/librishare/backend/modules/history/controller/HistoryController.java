package com.librishare.backend.modules.history.controller;

import com.librishare.backend.modules.history.dto.HistoryResponseDTO;
import com.librishare.backend.modules.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/history")
@Tag(name = "Histórico de Atividades", description = "Endpoints para consulta de histórico de empréstimos, trocas e devoluções de usuários")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(
            summary = "Lista o histórico completo de um usuário",
            description = "Retorna uma lista cronológica de todas as interações (empréstimos, devoluções, etc.) realizadas por um usuário específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = HistoryResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<HistoryResponseDTO>> getUserHistory(
            @Parameter(description = "ID do usuário proprietário do histórico", example = "1")
            @PathVariable Long userId) {

        List<HistoryResponseDTO> history = historyService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }
}