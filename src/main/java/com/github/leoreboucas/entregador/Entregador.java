package com.github.leoreboucas.entregador;

import com.github.leoreboucas.empresa.Empresa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_man")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entregador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "id_enterprise")
    private Empresa enterprise;
    private String firstName;
    private String secondName;
    @Column(unique = true)
    private String cpf;
    private String password;
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
    @Column(name = "cnh_category")
    @Enumerated(EnumType.STRING)
    private CategoriaCNH cnhCategory;
    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private TipoVeiculo vehicleType;
    @Enumerated(EnumType.STRING)
    private Disponibilidade availability;
    private double capacity;
    @Column(name = "delivery_man_type")
    @Enumerated(EnumType.STRING)
    private TipoEntregador deliveryManType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}