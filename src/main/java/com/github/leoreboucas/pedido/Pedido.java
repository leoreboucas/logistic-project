package com.github.leoreboucas.pedido;
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
    @Column(name = "customer_complete_name")
    private String customerCompleteName;
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
    @Column(name = "tracking_code")
    private String trackingCode;
    @Enumerated(EnumType.STRING)
    private PedidoStatus status;
    @Column(name = "delivery_attempts")
    private int deliveryAttempts = 0;
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