package com.github.leoreboucas.entregafinal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregaFinalRepository extends JpaRepository<EntregaFinal, Long> {
    EntregaFinal findTopByOrderTrackingCodeOrderByCreatedAtDesc(String trackingCode);
}
