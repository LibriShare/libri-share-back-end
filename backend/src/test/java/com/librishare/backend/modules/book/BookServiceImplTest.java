package com.librishare.backend.modules.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import com.librishare.backend.modules.book.repository.BookRepository;
import com.librishare.backend.modules.book.service.impl.BookServiceImpl;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookServiceImpl;

    @Test
    @DisplayName("Deve criar um livro com sucesso quando o usuário existe")
    void createBook_Success() {
        Long userId = 36L;
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setTitle("O Senhor dos Anéis");

        User mockUser = new User();
        mockUser.setId(userId);

        Book mockBookToSave = new Book();
        mockBookToSave.setTitle("O Senhor dos Anéis");

        Book mockSavedBook = new Book();
        mockSavedBook.setId(22L);
        mockSavedBook.setTitle("O Senhor dos Anéis");
        mockSavedBook.setUser(mockUser);

        BookResponseDTO expectedResponse = new BookResponseDTO();
        expectedResponse.setId(mockSavedBook.getId());
        expectedResponse.setTitle(mockSavedBook.getTitle());

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(modelMapper.map(requestDTO, Book.class)).thenReturn(mockBookToSave);
        when(bookRepository.save(any(Book.class))).thenReturn(mockSavedBook);
        when(modelMapper.map(mockSavedBook, BookResponseDTO.class)).thenReturn(expectedResponse);

        BookResponseDTO actualResponse = bookServiceImpl.createBook(requestDTO);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());

        verify(userRepository, times(1)).findById(userId);
        verify(modelMapper, times(1)).map(requestDTO, Book.class);
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(modelMapper, times(1)).map(mockSavedBook, BookResponseDTO.class);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o usuário não for encontrado")
    void createBook_UserNotFound() {
        Long nonExistentUserId = 2L;
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setUserId(nonExistentUserId);

        String expectedErrorMessage = "Usuário não encontrado com ID: " + nonExistentUserId;

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookServiceImpl.createBook(requestDTO);
        });

        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(bookRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), eq(Book.class));
        verify(modelMapper, never()).map(any(), eq(BookResponseDTO.class));
    }
}