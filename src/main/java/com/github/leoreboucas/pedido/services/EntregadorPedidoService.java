package com.github.leoreboucas.pedido.services;

import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.EntregaFinal;
import com.github.leoreboucas.entregafinal.EntregaFinalRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.entregaparcial.EntregaParcialRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.github.leoreboucas.pedido.PedidoStatus.*;

@Service
public class EntregadorPedidoService {
    private final EntregadorRepository entregadorRepository;
    private final EntregaParcialRepository entregaParcialRepository;
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoService historicoPedidoService;
    private final EntregaFinalRepository entregaFinalRepository;

    public EntregadorPedidoService(EntregadorRepository entregadorRepository, EntregaParcialRepository entregaParcialRepository, PedidoService pedidoService, PedidoRepository pedidoRepository, HistoricoPedidoService historicoPedidoService, EntregaFinalRepository entregaFinalRepository) {
        this.entregadorRepository = entregadorRepository;
        this.entregaParcialRepository = entregaParcialRepository;
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoService = historicoPedidoService;
        this.entregaFinalRepository = entregaFinalRepository;
    }


    public Pedido confirmDriverArrivalByDeliveryMan (String trackingCode, String cpf) {
        Entregador deliveryMan = entregadorRepository.findByCpf(cpf);

        if (deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }

        EntregaParcial partialDelivery = entregaParcialRepository.findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan);

        if(partialDelivery == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrega parcial não encontrada para o código de rastreamento e entregador informados! Verifique as informações e tente novamente.");
        }

        Pedido order = pedidoService.validationOnChangeStatus(partialDelivery.getDestinationCenter().getEnterprise().getCnpj(), trackingCode);

        switch (partialDelivery.getDestinationCenter().getCenterDistribuitionType()) {
            case TRANSACIONAL -> order.setStatus(EM_TRANSITO);
            case ULTIMA_MILHA -> order.setStatus(EM_DISTRIBUICAO);
        }

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, EM_TRANSITO, order.getStatus(), "Pedido chegou no centro de distribuição: " + partialDelivery.getDestinationCenter().getName());

        return order;
    }

    public Pedido registerAttemptByDeliveryMan (EntregaFinalStatus statusFinalDelivery, String trackingCode, String cpf) {
        Entregador deliveryMan = entregadorRepository.findByCpf(cpf);

        if (deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }
        EntregaFinal finalDelivery = entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);

        if(finalDelivery == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrega final não encontrada para o código de rastreamento informado! Verifique as informações e tente novamente.");
        }

        Pedido order = pedidoService.validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode);

        if(order.getStatus() != SAIU_PARA_ENTREGA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tentativa de entrega não pode ser registrada no status atual do pedido.");
        }

        PedidoStatus previousStatus  = order.getStatus();
        order.setDeliveryAttempts(order.getDeliveryAttempts() + 1);

        switch (statusFinalDelivery) {
            case FRACASSO -> {
                order.setStatus(EM_DISTRIBUICAO);
                 historicoPedidoService.registerOrderHistory(order, previousStatus, order.getStatus(), "Entrega do pedido falhou! Pedido retornou para Centro de distribuição.");
            }
            case SUCESSO -> {
                order.setStatus(ENTREGUE);
                historicoPedidoService.registerOrderHistory(order, previousStatus, order.getStatus(), "Pedido entregue com sucesso");
            }
        }

        pedidoRepository.save(order);

        return order;
    }
}
