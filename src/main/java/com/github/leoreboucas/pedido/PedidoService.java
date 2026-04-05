package com.github.leoreboucas.pedido;
import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.cliente.ClienteRepository;
import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.DTO.ListarPedidosDTO;
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
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService (PedidoRepository pedidoRepository, FornecedorRepository fornecedorRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.clienteRepository = clienteRepository;
    }


    public Pedido findByTrackingCode(String trackingCode) {
        Pedido pedido = pedidoRepository.findByTrackingCode(trackingCode);

        if (pedido == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }
        return pedido;
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

        pedidoRepository.save(newOrder);
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

        if(!order.getFornecedor().getCnpj().equals(cnpj)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não tem permissão para alterar status desse pedido.");
        }

        List<PedidoStatus> allowedStatus = List.of(
                PedidoStatus.AGUARDANDO_POSTAGEM,
                PedidoStatus.POSTADO,
                PedidoStatus.EM_TRIAGEM,
                PedidoStatus.EM_TRANSITO
        );

        if(!allowedStatus.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser cancelado no status atual.");
        }

        order.setStatus(PedidoStatus.CANCELADO);

        pedidoRepository.save(order);
        return order;
    }
}
