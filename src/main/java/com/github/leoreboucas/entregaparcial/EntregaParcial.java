package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.pedido.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "partial_delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EntregaParcial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "id_order")
    private Pedido order;
    @ManyToOne
    @JoinColumn(name = "id_delivery_man")
    private Entregador deliveryMan;
    @ManyToOne
    @JoinColumn(name = "id_origin_center")
    private CentroDistribuicao originCenter;
    @ManyToOne
    @JoinColumn(name = "id_destination_center")
    private CentroDistribuicao destinationCenter;
    @Column(name = "departure_date")
    private LocalDateTime departureDate;
    @Column(name = "created_date")
    private LocalDateTime createdAt;
    @Column(name = "deleted_date")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}