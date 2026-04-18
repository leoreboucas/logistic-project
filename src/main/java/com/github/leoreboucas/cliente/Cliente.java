package com.github.leoreboucas.cliente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String secondName;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(unique = true)
    private String cpf;
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    @Column(name = "cell_number")
    private String cellNumber;
    private String cep;
    private String street;
    private String complement;
    @Column(name = "house_number")
    private String houseNumber;
    private String neighborhood;
    private String city;
    private String state;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}