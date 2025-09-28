package com.librishare.backend.modules.user.service.impl;

import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.dto.UserResponseDTO;
import com.librishare.backend.modules.user.entity.User;
import com.librishare.backend.modules.user.repository.UserRepository;
import com.librishare.backend.modules.user.service.UserService;
import java.lang.reflect.Type;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Erro: Email já cadastrado.");
        }
        if (userRepository.findByCpf(userRequestDTO.getCpf()).isPresent()) {
            throw new RuntimeException("Erro: CPF já cadastrado.");
        }

        User newUser = mapper.map(userRequestDTO, User.class);

        User savedUser = userRepository.save(newUser);

        return mapper.map(savedUser, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        return mapper.map(user, UserResponseDTO.class);
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();

        Type listType = new TypeToken<List<UserResponseDTO>>() {}.getType();

        return mapper.map(users, listType);
    }

    @Override
    public void deleteUser(Long id) {
        // Verifica se o usuário existe antes de tentar deletar
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com o ID: " + id); // Use uma exceção personalizada
        }
        userRepository.deleteById(id);
    }
}