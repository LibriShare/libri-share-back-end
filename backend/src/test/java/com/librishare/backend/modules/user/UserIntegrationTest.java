package com.librishare.backend.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private UserRequestDTO createValidUserDTO(String cpf) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstName("Integration");
        dto.setLastName("Test");
        dto.setEmail("integration.test@example.com");
        dto.setPassword("ValidPassword123");
        dto.setCpf(cpf);
        return dto;
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso via API")
    void createUser_Success() throws Exception {
        UserRequestDTO userRequestDTO = createValidUserDTO("12345678901"); // CPF Simples

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("integration.test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Deve falhar ao criar usuário com email inválido (Validação)")
    void createUser_InvalidEmail_Fails() throws Exception {
        UserRequestDTO userRequestDTO = createValidUserDTO("12345678901");
        userRequestDTO.setEmail("invalid-email"); // Email inválido

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("O email fornecido é inválido"));
    }

    @Test
    @DisplayName("Deve falhar ao criar usuário com email duplicado (Conflito)")
    void createUser_DuplicateEmail_Fails() throws Exception {
        // 1. Cria o primeiro usuário
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidUserDTO("12345678901"))));

        // 2. Tenta criar o segundo usuário com o mesmo email
        UserRequestDTO duplicateUserDTO = createValidUserDTO("98765432109"); // CPF diferente

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Erro: Email já cadastrado."));
    }
}