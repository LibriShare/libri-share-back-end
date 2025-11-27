package com.librishare.backend.modules.user.dto;

import java.time.OffsetDateTime;
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
    private Integer annualReadingGoal;
    private String avatar;
    private OffsetDateTime createdAt;
}