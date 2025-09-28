package com.librishare.backend.modules.user.service;

import com.librishare.backend.modules.user.dto.UserRequestDTO;
import com.librishare.backend.modules.user.dto.UserResponseDTO;

import java.util.List;

/**
 * Interface que define o contrato para os serviços relacionados ao usuário.
 * Contém os métodos para as operações de negócio da entidade User.
 */
public interface UserService {

    /**
     * Cria um novo usuário no sistema.
     *
     * @param userCreateDTO DTO com os dados para a criação do usuário.
     * @return UserResponseDTO com os dados do usuário recém-criado.
     */
    UserResponseDTO createUser(UserRequestDTO userCreateDTO);

    /**
     * Busca um usuário pelo seu ID.
     *
     * @param id O ID do usuário a ser buscado.
     * @return UserResponseDTO com os dados do usuário encontrado.
     */
    UserResponseDTO findUserById(Long id);

    /**
     * Retorna uma lista com todos os usuários cadastrados.
     *
     * @return Lista de UserResponseDTO.
     */
    List<UserResponseDTO> findAllUsers();

    /**
     * Deleta um usuário do sistema com base no seu ID.
     *
     * @param id O ID do usuário a ser deletado.
     */
    void deleteUser(Long id);

}