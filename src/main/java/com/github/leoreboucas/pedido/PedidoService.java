package com.github.leoreboucas.pedido;
import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.cliente.ClienteRepository;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.entregaparcial.EntregaParcialService;
import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoRepository;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.DTO.ListarPedidosDTO;
import com.github.leoreboucas.rastreamento.DTO.HistoricoItemDTO;
import com.github.leoreboucas.rastreamento.DTO.RastreamentoResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final EmpresaRepository empresaRepository;
    private final EntregaParcialService entregaParcialService;

    public PedidoService (PedidoRepository pedidoRepository, FornecedorRepository fornecedorRepository, ClienteRepository clienteRepository, HistoricoPedidoRepository historicoPedidoRepository, FornecedorRepository fornecedorRepository1, HistoricoPedidoRepository historicoPedidoRepository1, EmpresaRepository empresaRepository, EntregaParcialService entregaParcialService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.historicoPedidoRepository = historicoPedidoRepository;
        this.empresaRepository = empresaRepository;
        this.entregaParcialService = entregaParcialService;
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

    public Pedido confirmPostByEnterprise (String trackingCode, String cnpj) {
        Pedido order = validationOnChangeStatusByEnterprise(cnpj, trackingCode, AGUARDANDO_POSTAGEM);

        order.setStatus(PedidoStatus.POSTADO);

        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setOrder(order);
        orderHistory.setPreviousStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
        orderHistory.setNewStatus(PedidoStatus.POSTADO);
        orderHistory.setObservation("Postagem do pedido confirmada.");
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

        return order;
    }

    public Pedido confirmScreeningByEnterpise (String trackingCode, String cnpj) {
        Pedido order = validationOnChangeStatusByEnterprise(cnpj, trackingCode, POSTADO);

        order.setStatus(EM_TRIAGEM);

        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setOrder(order);
        orderHistory.setPreviousStatus(POSTADO);
        orderHistory.setNewStatus(EM_TRIAGEM);
        orderHistory.setObservation("Triagem do pedido confirmada.");
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

        return order;
    }

    @Transactional
    public Pedido confirmShippingByEnterprise (EnviarPedidoDTO enviarPedidoDTO, String trackingCode, String cnpj) {
        Pedido order = validationOnChangeStatusByEnterprise(cnpj, trackingCode, EM_TRIAGEM);

        order.setStatus(EM_TRANSITO);

        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setOrder(order);
        orderHistory.setPreviousStatus(EM_TRIAGEM);
        orderHistory.setNewStatus(EM_TRANSITO);
        orderHistory.setObservation("Pedido enviado para centro de distribuição: " + enviarPedidoDTO.getDestinationCenter());
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);
        entregaParcialService.registerPartialDeliveryByEnterprise(enviarPedidoDTO, order, cnpj);

        return order;
    }

    private Pedido validationOnChangeStatusByEnterprise (String cnpj, String trackingCode, PedidoStatus actualStatus) {
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
        orderHistory.setObservation("Pedido cancelado.");
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

        return order;
    }

    public void updateStatusByPartialDelivery (EntregaParcial partialDelivery) {
        Pedido order = partialDelivery.getOrder();
        HistoricoPedido orderHistory = new HistoricoPedido();
        orderHistory.setPreviousStatus(order.getStatus());
        CentroDistribuicao distribuitionCenter = partialDelivery.getDestinationCenter();

        switch (distribuitionCenter.getCenterDistribuitionType()) {
            case TRANSACIONAL -> order.setStatus(EM_TRANSITO);
            case ULTIMA_MILHA -> order.setStatus(EM_DISTRIBUICAO);
        }
        switch (partialDelivery.getPartialDeliveryStatus()) {
            case AGUARDANDO_POSTAGEM -> order.setStatus(POSTADO);
            case POSTADO -> order.setStatus(EM_TRIAGEM);
            case EM_TRIAGEM -> order.setStatus(EM_TRANSITO);
            case EM_DISTRIBUICAO -> order.setStatus(SAIU_PARA_ENTREGA);
            case SAIU_PARA_ENTREGA -> order.setStatus(TENTATIVA_ENTREGA);
        }
        switch (distribuitionCenter.getCenterDistribuitionType()) {
            case TRANSACIONAL -> {

                switch (partialDelivery.getPartialDeliveryStatus()) {
                    case AGUARDANDO_POSTAGEM -> order.setStatus(POSTADO);
                    case POSTADO -> order.setStatus(EM_TRIAGEM);
                    case EM_TRIAGEM, EM_TRANSITO -> order.setStatus(EM_TRANSITO);
                    default -> throw new IllegalStateException("Valor inesperado: " + distribuitionCenter.getCenterDistribuitionType());
                }
            }
            case ULTIMA_MILHA -> {
                switch (partialDelivery.getPartialDeliveryStatus()) {
                    case EM_TRANSITO -> order.setStatus(EM_DISTRIBUICAO);
                    case EM_DISTRIBUICAO -> order.setStatus(SAIU_PARA_ENTREGA);
                    case SAIU_PARA_ENTREGA -> order.setStatus(TENTATIVA_ENTREGA);
                    case ENTREGUE -> order.setStatus(ENTREGUE);
                    case DEVOLVIDO -> order.setStatus(DEVOLVIDO);
                    default -> throw new IllegalStateException("Valor inesperado: " + distribuitionCenter.getCenterDistribuitionType());
                }
            }

        }
        orderHistory.setOrder(order);
        orderHistory.setNewStatus(order.getStatus());
        orderHistory.setObservation("Chegou em: " + distribuitionCenter.getName() + " às " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        orderHistory.setDateOfChange(LocalDateTime.now());

        pedidoRepository.save(order);
        historicoPedidoRepository.save(orderHistory);

    }
}
