package com.librishare.backend.modules.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.dto.UserResponseDTO;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import com.librishare.backend.modules.user.service.impl.UserServiceImpl;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirstName("John");
        userRequestDTO.setLastName("Doe");
        userRequestDTO.setEmail("john.doe@example.com");
        userRequestDTO.setCpf("12345678900");
        userRequestDTO.setPassword("pa222ssword123");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setCpf("12345678900");
        user.setPassword("password");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName("John");
        userResponseDTO.setLastName("Doe");
        userResponseDTO.setEmail("john.doe@example.com");
        userResponseDTO.setCpf("12345678900");
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(mapper.map(userRequestDTO, User.class)).thenReturn(user);
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userRequestDTO);

        assertNotNull(result);
        assertEquals(userResponseDTO.getId(), result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    @DisplayName("Deve encontrar um usuário por ID com sucesso")
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(userResponseDTO.getId(), result.getId());
        verify(mapper, times(1)).map(user, UserResponseDTO.class);
    }

    @Test
    @DisplayName("Deve retornar uma lista de todos os usuários")
    void findAllUsers_Success() {
        List<User> userList = Collections.singletonList(user);
        List<UserResponseDTO> userResponseList = Collections.singletonList(userResponseDTO);

        when(userRepository.findAll()).thenReturn(userList);

        Type listType = new TypeToken<List<UserResponseDTO>>() {}.getType();
        when(mapper.map(userList, listType)).thenReturn(userResponseList);

        List<UserResponseDTO> result = userService.findAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(userResponseDTO.getFirstName(), result.get(0).getFirstName());
        verify(mapper, times(1)).map(userList, listType); // Verifica se o mapper foi chamado
    }


    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void createUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(Optional.of(user));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(userRequestDTO));
        assertEquals("Erro: Email já cadastrado.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar usuário por ID")
    void findUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findUserById(99L));
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }
}