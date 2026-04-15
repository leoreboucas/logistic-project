package com.github.leoreboucas.pedido.services;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoRepository;
import com.github.leoreboucas.pedido.DTO.ListarPedidosDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import com.github.leoreboucas.rastreamento.DTO.HistoricoItemDTO;
import com.github.leoreboucas.rastreamento.DTO.RastreamentoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoRepository historicoPedidoRepository;
    private final EmpresaRepository empresaRepository;

    public PedidoService (PedidoRepository pedidoRepository, HistoricoPedidoRepository historicoPedidoRepository, EmpresaRepository empresaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoRepository = historicoPedidoRepository;
        this.empresaRepository = empresaRepository;
    }


    public RastreamentoResponseDTO getOrdersHistoryByTrackingCode(String trackingCode) {
        Pedido order = pedidoRepository.findByTrackingCode(trackingCode);

        if(order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }

        List<HistoricoPedido> ordersHistory = historicoPedidoRepository.findByOrderTrackingCode(trackingCode);

        RastreamentoResponseDTO rastreamentoResponseDTO = new RastreamentoResponseDTO();

        List<HistoricoItemDTO> itensHistory = ordersHistory.stream()
                .map(orderHistory -> new HistoricoItemDTO(
                        orderHistory.getPreviousStatus() != null ? orderHistory.getPreviousStatus().toString() : null,
                        orderHistory.getNewStatus().toString(),
                        orderHistory.getObservation(),
                        orderHistory.getDateOfChange()
                ))
                .collect(Collectors.toList());

        rastreamentoResponseDTO.setTrackingCode(trackingCode);
        rastreamentoResponseDTO.setStatus(order.getStatus().toString());
        rastreamentoResponseDTO.setOrdersHistory(itensHistory);
        return rastreamentoResponseDTO;
    }

    public String generateTrackingCode() {
        String trackingCodePartial = "LOG" + LocalDateTime.now().getYear() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Optional<Pedido> existingOrder = Optional.ofNullable(pedidoRepository.findByTrackingCode(trackingCodePartial));
        while (existingOrder.isPresent()) {
            trackingCodePartial = "LOG" + LocalDateTime.now().getYear() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            existingOrder = Optional.ofNullable(pedidoRepository.findByTrackingCode(trackingCodePartial));
        }

        return trackingCodePartial;
    }

    public Pedido validationOnChangeStatus (String cnpj, String trackingCode, PedidoStatus actualStatus) {
        Empresa enterprise = empresaRepository.findByCnpj(cnpj);

        if(enterprise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        Pedido order = pedidoRepository.findByTrackingCode(trackingCode);

        if(order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.");
        }

        if(order.getStatus() != actualStatus) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        return order;
    }

    public List<ListarPedidosDTO> getAllOrdersByCnpj (String cnpj) {
        List<Pedido> allOrders = pedidoRepository.findByFornecedorCnpj(cnpj);

        return allOrders.stream()
                .map(order -> new ListarPedidosDTO(
                        order.getTrackingCode(),
                        order.getStatus().toString(),
                        order.getForecastDelivery(),
                        order.getCliente().getFirstName() + " " + order.getCliente().getSecondName()
                ))
                .collect(Collectors.toList());
    }
}
