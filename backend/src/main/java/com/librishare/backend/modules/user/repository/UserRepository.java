package com.librishare.backend.modules.user.repository;

import com.librishare.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca um usuário pelo seu endereço de e-mail.
     * O Spring Data JPA cria a implementação deste método automaticamente.
     *
     * @param email O e-mail a ser pesquisado.
     * @return um Optional contendo o usuário, se encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca um usuário pelo seu CPF.
     * O Spring Data JPA cria a implementação deste método automaticamente.
     *
     * @param cpf O CPF a ser pesquisado.
     * @return um Optional contendo o usuário, se encontrado.
     */
    Optional<User> findByCpf(String cpf);

}