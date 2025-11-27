package com.librishare.backend.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String firstName;

    @NotBlank(message = "Sobrenome é obrigatório")
    private String lastName;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email fornecido é inválido")
    private String email;

    private String password;
    private String avatar;
    private Integer annualReadingGoal;
}