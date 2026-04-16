package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.entregador.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {
    EntregaParcial findByOrderTrackingCodeAndDeliveryMan(String trackingCode, Entregador deliveryMan);
}
