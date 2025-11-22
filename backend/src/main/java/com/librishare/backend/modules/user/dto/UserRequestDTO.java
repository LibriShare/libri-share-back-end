package com.librishare.backend.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "O primeiro nome é obrigatório")
    @Size(min = 2, message = "O primeiro nome deve ter pelo menos 2 caracteres")
    private String firstName;

    @NotBlank(message = "O último nome é obrigatório")
    @Size(min = 2, message = "O último nome deve ter pelo menos 2 caracteres")
    private String lastName;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email fornecido é inválido")
    private String email;

    // REMOVIDO @NotBlank DAQUI PARA PERMITIR EDIÇÃO DE PERFIL SEM MUDAR SENHA
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "O CPF deve conter exatamente 11 dígitos numéricos")
    private String cpf;

    private LocalDate dateOfBirth;
    private String biography;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressZip;
    private Integer annualReadingGoal;
}