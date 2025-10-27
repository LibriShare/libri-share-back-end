package com.librishare.backend.modules.book.service.impl;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.book.service.BookService;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BookResponseDTO createBook(BookRequestDTO requestDTO) {
        // 1. Verificar se o usuário existe
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + requestDTO.getUserId()));

        // 2. Mapear o DTO para a Entidade Book
        Book book = modelMapper.map(requestDTO, Book.class);

        // 3. Associar o usuário ao livro
        book.setUser(user);
        
        // 4. Salvar o livro no banco
        Book savedBook = bookRepository.save(book);

        // 5. Mapear a entidade salva para o DTO de resposta
        return modelMapper.map(savedBook, BookResponseDTO.class);
    }
}