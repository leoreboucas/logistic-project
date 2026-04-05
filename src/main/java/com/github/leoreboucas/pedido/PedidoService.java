package com.github.leoreboucas.pedido;
import com.github.leoreboucas.cliente.Cliente;
import com.github.leoreboucas.cliente.ClienteRepository;
import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.infra.security.JwtFilter;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        newOrder.setStatus("AGUARDANDO_POSTAGEM");
        newOrder.setWeight(criarPedidoDTO.getWeight());
        newOrder.setLength(criarPedidoDTO.getLength());
        newOrder.setWidth(criarPedidoDTO.getWidth());
        newOrder.setDepth(criarPedidoDTO.getDepth());
        newOrder.setObservation(criarPedidoDTO.getObservation());

        pedidoRepository.save(newOrder);

        return newOrder;
    }
}
