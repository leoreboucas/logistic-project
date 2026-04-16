package com.github.leoreboucas.pedido.services;

import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.cliente.ClienteRepository;
import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.leoreboucas.pedido.PedidoStatus.EM_TRIAGEM;

@Service
public class FornecedorPedidoService {
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoService historicoPedidoService;
    private final PedidoService pedidoService;

    public FornecedorPedidoService(FornecedorRepository fornecedorRepository, ClienteRepository clienteRepository, PedidoRepository pedidoRepository, HistoricoPedidoService historicoPedidoService, PedidoService pedidoService) {
        this.fornecedorRepository = fornecedorRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoService = historicoPedidoService;
        this.pedidoService = pedidoService;
    }


    public Pedido createOrderBySupplier(CriarPedidoDTO criarPedidoDTO, String cnpj) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(cnpj);

        if(supplier == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        String trackingCode = pedidoService.generateTrackingCode();

        Pedido newOrder = new Pedido();
        newOrder.setFornecedor(supplier);
        newOrder.setCellNumber(criarPedidoDTO.getCellNumber());
        newOrder.setCustomerCompleteName(criarPedidoDTO.getCustomerCompleteName());
        newOrder.setCep(criarPedidoDTO.getCep());
        newOrder.setHouseNumber(criarPedidoDTO.getHouseNumber());
        newOrder.setStreet(criarPedidoDTO.getStreet());
        newOrder.setNeighborhood(criarPedidoDTO.getNeighborhood());
        newOrder.setComplement(criarPedidoDTO.getComplement());
        newOrder.setCity(criarPedidoDTO.getCity());
        newOrder.setState(criarPedidoDTO.getState());
        newOrder.setTrackingCode(trackingCode);
        newOrder.setStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
        newOrder.setWeight(criarPedidoDTO.getWeight());
        newOrder.setLength(criarPedidoDTO.getLength());
        newOrder.setWidth(criarPedidoDTO.getWidth());
        newOrder.setDepth(criarPedidoDTO.getDepth());
        newOrder.setObservation(criarPedidoDTO.getObservation());

        pedidoRepository.save(newOrder);
        historicoPedidoService.registerOrderHistory(newOrder, null, PedidoStatus.AGUARDANDO_POSTAGEM, null);
        return newOrder;
    }

    public Pedido cancelOrderBySupplier (String trackingCode, String cnpj) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(cnpj);

        if(supplier == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        Pedido order = pedidoRepository.findByTrackingCode(trackingCode);
        PedidoStatus previousStatus = order.getStatus();

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

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, previousStatus, PedidoStatus.CANCELADO, "Pedido cancelado pelo fornecedor.");

        return order;
    }
}
