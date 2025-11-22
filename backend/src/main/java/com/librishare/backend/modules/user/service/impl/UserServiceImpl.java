package com.librishare.backend.modules.user.service.impl;

import com.librishare.backend.exception.DuplicateResourceException;
import com.librishare.backend.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetado

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Erro: Email já cadastrado.");
        }
        if (userRepository.findByCpf(userRequestDTO.getCpf()).isPresent()) {
            throw new DuplicateResourceException("Erro: CPF já cadastrado.");
        }

        User newUser = mapper.map(userRequestDTO, User.class);

        newUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        User savedUser = userRepository.save(newUser);

        return mapper.map(savedUser, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        return mapper.map(user, UserResponseDTO.class);
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        Type listType = new TypeToken<List<UserResponseDTO>>() {}.getType();
        return mapper.map(users, listType);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        // Validações de duplicidade (mantém código anterior...)
        userRepository.findByEmail(userRequestDTO.getEmail()).ifPresent(user -> {
            if (!user.getId().equals(id)) throw new DuplicateResourceException("Erro: Email já cadastrado por outro usuário.");
        });
        userRepository.findByCpf(userRequestDTO.getCpf()).ifPresent(user -> {
            if (!user.getId().equals(id)) throw new DuplicateResourceException("Erro: CPF já cadastrado por outro usuário.");
        });

        // CORREÇÃO: Salvar senha antiga antes do mapeamento
        String oldPassword = userToUpdate.getPassword();

        // Mapeia os campos do DTO para a entidade existente
        mapper.map(userRequestDTO, userToUpdate);

        // Lógica de Senha:
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
            // Se enviou nova senha, criptografa
            userToUpdate.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        } else {
            // Se não enviou senha, mantém a antiga (evita que o mapper deixe null)
            userToUpdate.setPassword(oldPassword);
        }

        User updatedUser = userRepository.save(userToUpdate);
        return mapper.map(updatedUser, UserResponseDTO.class);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        userRepository.deleteById(id);
    }
}