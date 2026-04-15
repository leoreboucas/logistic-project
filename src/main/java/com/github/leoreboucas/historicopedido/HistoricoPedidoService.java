package com.github.leoreboucas.historicopedido;

import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoricoPedidoService {
    private final HistoricoPedidoRepository historicoPedidoRepository;

    public HistoricoPedidoService(HistoricoPedidoRepository historicoPedidoRepository) {
        this.historicoPedidoRepository = historicoPedidoRepository;
    }

    public void registerOrderHistory(Pedido order, PedidoStatus previousStatus, PedidoStatus newStatus, String observation) {
        HistoricoPedido history = new HistoricoPedido();
        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setObservation(observation);
        history.setDateOfChange(LocalDateTime.now());
        historicoPedidoRepository.save(history);
    }
}
