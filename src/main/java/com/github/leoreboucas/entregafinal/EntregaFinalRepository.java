package com.github.leoreboucas.entregafinal;

import com.github.leoreboucas.entregador.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregaFinalRepository extends JpaRepository<EntregaFinal, Long> {
    EntregaFinal findTopByOrderTrackingCodeOrderByCreatedAtDesc(String trackingCode);
    List<EntregaFinal> findByDeliveryMan(Entregador deliveryMan);
}
