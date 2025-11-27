package com.librishare.backend.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String avatar;

    private Integer annualReadingGoal;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}