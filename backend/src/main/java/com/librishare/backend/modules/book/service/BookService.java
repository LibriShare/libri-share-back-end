package com.librishare.backend.modules.book.service;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;

public interface BookService {
    
    /**
     * Cria um novo livro e o associa a um usuário.
     * @param requestDTO Os dados do livro a ser criado.
     * @return O livro criado.
     */
    BookResponseDTO createBook(BookRequestDTO requestDTO);
}