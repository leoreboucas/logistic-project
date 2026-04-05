package com.github.leoreboucas.historicopedido;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPedidoRepository extends JpaRepository<HistoricoPedido, Long> {
    List<HistoricoPedido> findByOrderTrackingCode(String trackingCode);
}
