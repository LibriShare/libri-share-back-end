package com.librishare.backend.modules.history;

import com.librishare.backend.modules.history.entity.UserHistory;
import com.librishare.backend.modules.history.repository.UserHistoryRepository;
import com.librishare.backend.modules.user.entity.User;
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

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HistoryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserHistoryRepository historyRepository;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder().firstName("Hist").lastName("User").email("hist@test.com").build());
    }

    @Test
    @DisplayName("Deve recuperar o histórico de atividades do usuário")
    void getUserHistory_Success() throws Exception {
        historyRepository.save(UserHistory.builder()
                .user(user)
                .actionType("LOGIN")
                .description("Login realizado")
                .createdAt(OffsetDateTime.now().minusHours(2))
                .build());

        historyRepository.save(UserHistory.builder()
                .user(user)
                .actionType("BIBLIOTECA")
                .description("Adicionou livro X")
                .createdAt(OffsetDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].actionType", is("BIBLIOTECA")))
                .andExpect(jsonPath("$[1].actionType", is("LOGIN")));
    }

    @Test
    @DisplayName("Deve retornar 404 se usuário não existir")
    void getUserHistory_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/99999/history"))
                .andExpect(status().isNotFound());
    }
}