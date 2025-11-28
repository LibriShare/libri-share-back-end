package com.librishare.backend.modules.user;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.user.dto.LoginRequestDTO;
import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.dto.UserResponseDTO;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import com.librishare.backend.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirstName("John");
        userRequestDTO.setLastName("Doe");
        userRequestDTO.setEmail("john.doe@example.com");
        userRequestDTO.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encoded_password_123");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName("John");
        userResponseDTO.setLastName("Doe");
        userResponseDTO.setEmail("john.doe@example.com");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("john.doe@example.com");
        loginRequestDTO.setPassword("password123");
    }

    // --- Create Tests ---

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(mapper.map(userRequestDTO, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password_123");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userRequestDTO);

        assertNotNull(result);
        assertEquals(userResponseDTO.getId(), result.getId());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void createUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(userRequestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Find By ID Tests ---

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar usuário por ID")
    void findUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(99L));
    }

    // --- Find All Tests ---

    @Test
    @DisplayName("Deve listar todos os usuários")
    void findAllUsers_Success() {
        List<User> userList = Collections.singletonList(user);
        when(userRepository.findAll()).thenReturn(userList);
        when(mapper.map(eq(userList), any(Type.class))).thenReturn(Collections.singletonList(userResponseDTO));

        List<UserResponseDTO> result = userService.findAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // --- Update Tests ---

    @Test
    @DisplayName("Deve atualizar usuário com sucesso (trocando senha)")
    void updateUser_Success_NewPassword() {
        UserRequestDTO updateRequest = new UserRequestDTO();
        updateRequest.setEmail("john.doe@example.com");
        updateRequest.setPassword("new_password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        doNothing().when(mapper).map(any(UserRequestDTO.class), any(User.class));

        when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(mapper.map(any(User.class), eq(UserResponseDTO.class))).thenReturn(userResponseDTO);

        userService.updateUser(1L, updateRequest);

        verify(passwordEncoder).encode("new_password");
        verify(userRepository).save(user);

        verify(mapper).map(any(UserRequestDTO.class), any(User.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar atualizar com email de outro usuário")
    void updateUser_EmailConflict() {
        User otherUser = new User();
        otherUser.setId(2L); // Outro ID
        otherUser.setEmail("jane@example.com");

        UserRequestDTO updateRequest = new UserRequestDTO();
        updateRequest.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(1L, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar atualizar usuário inexistente")
    void updateUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, userRequestDTO));
    }

    // --- Delete Tests ---

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar erro ao deletar usuário inexistente")
    void deleteUser_NotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    // --- Find By Email Tests ---

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void findUserByEmail_Success() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.findUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Deve lançar erro se email não encontrado")
    void findUserByEmail_NotFound() {
        when(userRepository.findByEmail("invalid@email.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserByEmail("invalid@email.com"));
    }

    // --- Login Tests ---

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_Success() {
        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));
        // matches(rawPassword, encodedPassword)
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(mapper.map(user, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.login(loginRequestDTO);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Deve falhar login se senha incorreta")
    void login_WrongPassword() {
        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequestDTO));
    }

    @Test
    @DisplayName("Deve falhar login se email inexistente")
    void login_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequestDTO));
    }
}