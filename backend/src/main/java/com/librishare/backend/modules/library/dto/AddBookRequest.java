package com.librishare.backend.modules.library.dto;

import com.librishare.backend.modules.library.enums.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookRequest {
    
    @NotNull(message = "O ID do livro é obrigatório")
    private Long bookId;

    @NotNull(message = "O status de leitura é obrigatório")
    private ReadingStatus status;
}