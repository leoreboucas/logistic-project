package com.github.leoreboucas.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Pedido findByTrackingCode(String trackingCode);
}
