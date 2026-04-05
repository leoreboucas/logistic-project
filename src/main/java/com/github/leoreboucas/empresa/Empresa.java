package com.github.leoreboucas.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String password;
    @Column(unique = true)
    private String cnpj;
    @Column(name = "cell_number")
    private String cellNumber;
    private String cep;
    private String street;
    @Column(name = "house_number")
    private String houseNumber;
    private String complement;
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
