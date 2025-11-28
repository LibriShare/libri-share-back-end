package com.librishare.backend.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.modules.user.dto.LoginRequestDTO;
import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createUser_Success() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstName("Maria");
        dto.setLastName("Silva");
        dto.setEmail("maria@email.com");
        dto.setPassword("123456");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_Success() throws Exception {
        User user = new User();
        user.setFirstName("João");
        user.setLastName("Teste");
        user.setEmail("joao@login.com");
        user.setPassword(passwordEncoder.encode("senha123"));
        userRepository.save(user);

        LoginRequestDTO loginDto = new LoginRequestDTO();
        loginDto.setEmail("joao@login.com");
        loginDto.setPassword("senha123");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@login.com"));
    }

    @Test
    @DisplayName("Deve falhar login com senha incorreta")
    void login_WrongPassword() throws Exception {
        User user = new User();
        user.setFirstName("João");
        user.setLastName("Teste");
        user.setEmail("joao@errado.com");
        user.setPassword(passwordEncoder.encode("senhaCorreta"));
        userRepository.save(user);

        LoginRequestDTO loginDto = new LoginRequestDTO();
        loginDto.setEmail("joao@errado.com");
        loginDto.setPassword("senhaErrada");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized()); // BadCredentialsException vira 401 no GlobalHandler
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void getUserById_Success() throws Exception {
        User user = userRepository.save(User.builder()
                .firstName("Ana").lastName("Souza").email("ana@id.com").password("pw").build());

        mockMvc.perform(get("/api/v1/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }
}