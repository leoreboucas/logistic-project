package com.github.leoreboucas.entregafinal;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.pedido.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntregaFinal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Pedido order;
    @ManyToOne
    @JoinColumn(name = "delivery_man_id")
    private Entregador deliveryMan;
    @ManyToOne
    @JoinColumn(name = "origin_center_id")
    private CentroDistribuicao originCenter;
    @Column(name = "departure_date")
    private LocalDateTime departureDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}