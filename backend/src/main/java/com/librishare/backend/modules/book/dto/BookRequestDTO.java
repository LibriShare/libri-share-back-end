package com.librishare.backend.modules.book.dto;

import com.librishare.backend.modules.book.enums.BookStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Data
public class BookRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    private String title;

    @NotBlank(message = "O autor é obrigatório")
    private String author;

    private String isbn;
    private String genre;

    @Min(value = 1, message = "O número de páginas deve ser positivo")
    private Integer totalPages;
    
    private Integer publicationYear;
    private String publisher;

    @URL(message = "A URL da capa deve ser válida")
    private String coverUrl;

    @NotNull(message = "O status é obrigatório")
    private BookStatus status;

    @Min(value = 0, message = "Páginas lidas não pode ser negativo")
    private Integer pagesRead = 0;

    @Min(value = 0, message = "A avaliação mínima é 0")
    @Max(value = 5, message = "A avaliação máxima é 5")
    private Integer rating = 0;

    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;
}