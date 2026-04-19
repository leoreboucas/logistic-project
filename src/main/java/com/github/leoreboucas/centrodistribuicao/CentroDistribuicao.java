package com.github.leoreboucas.centrodistribuicao;

import com.github.leoreboucas.empresa.Empresa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "distribution_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CentroDistribuicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "id_enterprise")
    private Empresa enterprise;
    private String name;
    private String cep;
    private String street;
    @Enumerated(EnumType.STRING)
    @Column(name = "center_distribuition_type")
    private TipoCentroDistribuicao centerDistribuitionType;
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
