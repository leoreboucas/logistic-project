package com.github.leoreboucas.pedido;

import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.fornecedor.Fornecedor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "id_supplier")
    private Fornecedor fornecedor;
    @ManyToOne
    @JoinColumn(name = "id_costumer")
    private Cliente cliente;
    @Column(name = "tracking_code")
    private String trackingCode;
    private String status;
    private double weight;
    private double length;
    private double width;
    private double depth;
    private String observation;
    @Column(name = "forecast_delivery")
    private LocalDateTime forecastDelivery;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}