package com.github.leoreboucas.entregaparcial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {
    List<EntregaParcial> findByOrderTrackingCode(String trackingCode);
}
