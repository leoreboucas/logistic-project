package com.github.leoreboucas.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Pedido findByTrackingCode(String trackingCode);
    List<Pedido> findByFornecedorCnpj(String cnpj);
}
