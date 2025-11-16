package com.librishare.backend.modules.user.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String cpf;
  private LocalDate dateOfBirth;
  private String biography;
  private String addressStreet;
  private String addressCity;
  private String addressState;
  private String addressZip;
  private Integer annualReadingGoal;
}