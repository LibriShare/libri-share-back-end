package com.librishare.backend.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
import com.librishare.backend.modules.user.controller.UserController;
import com.librishare.backend.modules.user.dto.LoginRequestDTO;
import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.dto.UserResponseDTO;
import com.librishare.backend.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new UserTestControllerAdvice())
                .build();

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirstName("Bianca");
        userRequestDTO.setLastName("Dev");
        userRequestDTO.setEmail("bianca@example.com");
        userRequestDTO.setPassword("senhaForte123");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName("Bianca");
        userResponseDTO.setLastName("Dev");
        userResponseDTO.setEmail("bianca@example.com");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("bianca@example.com");
        loginRequestDTO.setPassword("senhaForte123");
    }

    // --- CREATE (POST) ---

    @Test
    @DisplayName("Deve criar usuário com sucesso (201)")
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("bianca@example.com")));
    }

    @Test
    @DisplayName("Deve retornar 400 se dados inválidos (ex: email vazio)")
    void createUser_InvalidData() throws Exception {
        UserRequestDTO invalidUser = new UserRequestDTO();
        // Campos nulos vão disparar o @Valid

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 409 se email ou CPF já existem")
    void createUser_Duplicate() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Email já cadastrado"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isConflict()); // Graças ao UserTestControllerAdvice
    }

    // --- LIST (GET) ---

    @Test
    @DisplayName("Deve listar todos os usuários (200)")
    void getAllUsers_Success() throws Exception {
        List<UserResponseDTO> users = Collections.singletonList(userResponseDTO);
        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Bianca")));
    }

    // --- GET BY ID ---

    @Test
    @DisplayName("Deve buscar usuário por ID (200)")
    void getUserById_Success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Deve retornar 404 se ID não encontrado")
    void getUserById_NotFound() throws Exception {
        when(userService.findUserById(99L))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/api/v1/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    // --- SEARCH BY EMAIL ---

    @Test
    @DisplayName("Deve buscar usuário por Email (200)")
    void getUserByEmail_Success() throws Exception {
        when(userService.findUserByEmail("bianca@example.com")).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/v1/users/search")
                        .param("email", "bianca@example.com") // Query Param
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("bianca@example.com")));
    }

    // --- UPDATE (PUT) ---

    @Test
    @DisplayName("Deve atualizar usuário (200)")
    void updateUser_Success() throws Exception {
        // Precisamos enviar um objeto válido, mesmo no update, por causa do @Valid
        when(userService.updateUser(eq(1L), any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Bianca")));
    }

    // --- DELETE ---

    @Test
    @DisplayName("Deve deletar usuário (204)")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());
        
        verify(userService, times(1)).deleteUser(1L);
    }

    // --- LOGIN (POST) ---

    @Test
    @DisplayName("Deve realizar login com sucesso (200)")
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("bianca@example.com")));
    }

    @Test
    @DisplayName("Deve retornar 401 (ou erro equivalente) se credenciais inválidas")
    void login_Unauthorized() throws Exception {
        // Simulando uma exceção de negócio para login inválido
        // Ajuste 'IllegalArgumentException' para a exceção real que sua Service lança (ex: BadCredentialsException)
        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Credenciais inválidas"));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isUnauthorized()); // Mapeado no Advice abaixo
    }

    /**
     * Classe auxiliar interna para mapear Exceções -> HTTP Status
     * Isso permite testar os cenários de erro sem precisar carregar o contexto inteiro do Spring Boot.
     */
    @RestControllerAdvice
    static class UserTestControllerAdvice {
        
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Void> handleNotFound() {
            return ResponseEntity.notFound().build();
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<Void> handleConflict() {
            return ResponseEntity.status(409).build();
        }

        // Mapeando erro de login para 401 Unauthorized
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Void> handleBadCredentials() {
            return ResponseEntity.status(401).build();
        }
    }
}