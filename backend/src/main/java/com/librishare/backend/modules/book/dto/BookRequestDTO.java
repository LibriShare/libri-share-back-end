package com.librishare.backend.modules.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    private String title;

    @NotBlank(message = "O autor é obrigatório")
    private String author;

    private String publisher;
    private Integer publicationYear;

    @Size(min = 10, max = 13, message = "ISBN deve ter entre 10 e 13 caracteres")
    private String isbn;

    private Integer pages;

    private String coverImageUrl;

    private String googleBooksId;

    private String synopsis;

    private BigDecimal price;

    private String purchaseUrl;

}