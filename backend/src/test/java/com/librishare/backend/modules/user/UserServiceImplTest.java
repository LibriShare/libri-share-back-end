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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        // Dados padrão para testes não parametrizados
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

    @ParameterizedTest
    @CsvSource({
            "John, Doe, john.doe@example.com, password123",
            "Maria, Silva, maria.silva@test.com, 123456",
            "Admin, User, admin@system.org, strongPass!"
    })
    @DisplayName("Deve criar utilizadores com diferentes dados com sucesso")
    void createUser_Parameterized(String firstName, String lastName, String email, String password) {
        // Arrange
        UserRequestDTO req = new UserRequestDTO();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setEmail(email);
        req.setPassword(password);

        User mappedUser = new User();
        mappedUser.setFirstName(firstName);
        mappedUser.setEmail(email);
        mappedUser.setPassword("encoded_" + password);

        UserResponseDTO expectedResponse = new UserResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setFirstName(firstName);
        expectedResponse.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(mapper.map(req, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode(password)).thenReturn("encoded_" + password);
        when(userRepository.save(any(User.class))).thenReturn(mappedUser);
        when(mapper.map(mappedUser, UserResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        UserResponseDTO result = userService.createUser(req);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(firstName, result.getFirstName());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void createUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(userRequestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Find By ID Tests ---

    @ParameterizedTest
    @ValueSource(longs = { 1L, 100L, 5000L })
    @DisplayName("Deve buscar utilizador por vários IDs válidos com sucesso")
    void findUserById_Parameterized(Long id) {
        // Arrange
        User mockUser = new User();
        mockUser.setId(id);

        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));
        when(mapper.map(mockUser, UserResponseDTO.class)).thenReturn(mockResponse);

        // Act
        UserResponseDTO result = userService.findUserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(userRepository).findById(id);
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

    @ParameterizedTest
    @ValueSource(longs = { 1L, 2L, 99L })
    @DisplayName("Deve deletar utilizador com sucesso para diferentes IDs")
    void deleteUser_Parameterized(Long id) {
        // Arrange
        when(userRepository.existsById(id)).thenReturn(true);

        // Act
        userService.deleteUser(id);

        // Assert
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar erro ao deletar usuário inexistente")
    void deleteUser_NotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    // --- Find By Email Tests ---

    @ParameterizedTest
    @ValueSource(strings = { "john.doe@example.com", "jane@test.co.uk", "admin-user@domain.org" })
    @DisplayName("Deve buscar utilizador por diferentes emails válidos")
    void findUserByEmail_Parameterized(String email) {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail(email);

        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(mapper.map(mockUser, UserResponseDTO.class)).thenReturn(mockResponse);

        // Act
        UserResponseDTO result = userService.findUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar erro se email não encontrado")
    void findUserByEmail_NotFound() {
        when(userRepository.findByEmail("invalid@email.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserByEmail("invalid@email.com"));
    }

    // --- Login Tests ---

    @ParameterizedTest
    @CsvSource({
            "john.doe@example.com, password123",
            "user2@test.com, mySecretPass"
    })
    @DisplayName("Deve realizar login com sucesso para diferentes credenciais")
    void login_Parameterized(String email, String password) {
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail(email);
        loginReq.setPassword(password);

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword("encoded_pass");

        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(password, mockUser.getPassword())).thenReturn(true);
        when(mapper.map(mockUser, UserResponseDTO.class)).thenReturn(mockResponse);

        // Act
        UserResponseDTO result = userService.login(loginReq);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(passwordEncoder).matches(password, mockUser.getPassword());
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