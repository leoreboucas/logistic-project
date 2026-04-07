package com.github.leoreboucas.pedido;
import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.cliente.ClienteRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoRepository;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.DTO.ListarPedidosDTO;
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

import static com.github.leoreboucas.pedido.PedidoStatus.*;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoPedidoRepository historicoPedidoRepository;

    public PedidoService (PedidoRepository pedidoRepository, FornecedorRepository fornecedorRepository, ClienteRepository clienteRepository, HistoricoPedidoRepository historicoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.clienteRepository = clienteRepository;
        this.historicoPedidoRepository = historicoPedidoRepository;
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

        if(existingOrder.isPresent()){
            return generateTrackingCode();
        }
        return trackingCodePartial;
    }

    public Pedido createOrderBySupplier(CriarPedidoDTO criarPedidoDTO, String cnpj) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(cnpj);

        if(supplier == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        Cliente costumer = clienteRepository.findByCpf(criarPedidoDTO.getCpfCliente());

        if(costumer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
        }

        String trackingCode = generateTrackingCode();

        Pedido newOrder = new Pedido();
        newOrder.setFornecedor(supplier);
        newOrder.setCliente(costumer);
        newOrder.setTrackingCode(trackingCode);
        newOrder.setStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
        newOrder.setWeight(criarPedidoDTO.getWeight());
        newOrder.setLength(criarPedidoDTO.getLength());
        newOrder.setWidth(criarPedidoDTO.getWidth());
        newOrder.setDepth(criarPedidoDTO.getDepth());
        newOrder.setObservation(criarPedidoDTO.getObservation());

        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setOrder(newOrder);
        orderHistory.setPreviousStatus(null);
        orderHistory.setNewStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
        orderHistory.setObservation(null);
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(newOrder);
        historicoPedidoRepository.save(orderHistory);

        return newOrder;
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

    public Pedido cancelOrderBySupplier (String trackingCode, String cnpj) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(cnpj);

        if(supplier == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        Pedido order = pedidoRepository.findByTrackingCode(trackingCode);
        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setPreviousStatus(order.getStatus());

        if(!order.getFornecedor().getCnpj().equals(cnpj)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não tem permissão para alterar status desse pedido.");
        }

        List<PedidoStatus> allowedStatus = List.of(
                PedidoStatus.AGUARDANDO_POSTAGEM,
                PedidoStatus.POSTADO,
                EM_TRIAGEM,
                PedidoStatus.EM_TRANSITO
        );

        if(!allowedStatus.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser cancelado no status atual.");
        }

        order.setStatus(PedidoStatus.CANCELADO);

        orderHistory.setOrder(order);
        orderHistory.setNewStatus(PedidoStatus.CANCELADO);
        orderHistory.setObservation(null);
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

        return order;
    }

    public Pedido updateStatusByPartialDelivery (EntregaParcial partialDelivery) {
        Pedido order = partialDelivery.getOrder();
        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setPreviousStatus(order.getStatus());

        switch (partialDelivery.getPartialDeliveryStatus()) {
            case EM_TRANSITO -> order.setStatus(PedidoStatus.EM_TRANSITO);
            case ENTREGUE -> order.setStatus(ENTREGUE);
            case DEVOLVIDO -> order.setStatus(PedidoStatus.DEVOLVIDO);
        }

        orderHistory.setOrder(order);
        orderHistory.setNewStatus(order.getStatus());
        orderHistory.setObservation("Atualização automática de status por entrega parcial.");
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

        return order;

    }
}
