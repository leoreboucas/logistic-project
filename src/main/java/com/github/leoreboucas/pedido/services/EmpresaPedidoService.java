package com.github.leoreboucas.pedido.services;

import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcialService;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.github.leoreboucas.pedido.PedidoStatus.*;
import static com.github.leoreboucas.pedido.PedidoStatus.EM_TRANSITO;
import static com.github.leoreboucas.pedido.PedidoStatus.EM_TRIAGEM;
import static com.github.leoreboucas.pedido.PedidoStatus.POSTADO;

@Service
public class EmpresaPedidoService {
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoService historicoPedidoService;
    private final EntregaParcialService entregaParcialService;
    private final EntregadorRepository entregadorRepository;


    public EmpresaPedidoService(PedidoService pedidoService, PedidoRepository pedidoRepository, HistoricoPedidoService historicoPedidoService, EntregaParcialService entregaParcialService, EntregadorRepository entregadorRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoService = historicoPedidoService;
        this.entregaParcialService = entregaParcialService;
        this.entregadorRepository = entregadorRepository;
    }

    public Pedido confirmPostByEnterprise (String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode, AGUARDANDO_POSTAGEM);

        order.setStatus(PedidoStatus.POSTADO);

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, PedidoStatus.AGUARDANDO_POSTAGEM, PedidoStatus.POSTADO, "Postagem do pedido confirmada.");

        return order;
    }

    public Pedido confirmScreeningByEnterpise (String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode, POSTADO);

        order.setStatus(EM_TRIAGEM);

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, POSTADO, EM_TRIAGEM, "Triagem do pedido confirmada.");

        return order;
    }

    @Transactional
    public Pedido confirmShippingByEnterprise (EnviarPedidoDTO enviarPedidoDTO, String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode, PedidoStatus.valueOf(enviarPedidoDTO.getActualStatus()));
//
        if(order.getStatus().equals(EM_TRIAGEM)) {
            order.setStatus(EM_TRANSITO);
        } else if (order.getStatus().equals(EM_TRANSITO)) {
            order.setStatus(EM_DISTRIBUICAO);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, PedidoStatus.valueOf(enviarPedidoDTO.getActualStatus()), order.getStatus(), "Pedido enviado para centro de distribuição: " + enviarPedidoDTO.getDestinationCenter());
        entregaParcialService.registerPartialDeliveryByEnterprise(enviarPedidoDTO, order, cnpj);

        return order;
    }

    public Pedido outForDeliveryByEnterprise(EnviarPedidoDTO enviarPedidoDTO, String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode, EM_DISTRIBUICAO);

        order.setStatus(SAIU_PARA_ENTREGA);
        Entregador deliveryMan = entregadorRepository.findByCpf(enviarPedidoDTO.getDeliveryManCpf());

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, EM_DISTRIBUICAO, SAIU_PARA_ENTREGA, "Pedido saiu para entrega. Entregador: " + deliveryMan.getFirstName() + " " + deliveryMan.getSecondName());
        entregaParcialService.registerPartialDeliveryByEnterprise(enviarPedidoDTO, order, cnpj);

        return order;
    }
}
