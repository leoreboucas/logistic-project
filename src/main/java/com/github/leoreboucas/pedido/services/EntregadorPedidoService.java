package com.github.leoreboucas.pedido.services;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.entregaparcial.EntregaParcialRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcialService;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.ConfirmarChegadaDTO;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.github.leoreboucas.pedido.PedidoStatus.EM_DISTRIBUICAO;
import static com.github.leoreboucas.pedido.PedidoStatus.EM_TRANSITO;

@Service
public class EntregadorPedidoService {
    private final EntregadorRepository entregadorRepository;
    private final EntregaParcialRepository entregaParcialRepository;
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoService historicoPedidoService;
    private final EntregaParcialService entregaParcialService;

    public EntregadorPedidoService(EntregadorRepository entregadorRepository, EntregaParcialRepository entregaParcialRepository, PedidoService pedidoService, PedidoRepository pedidoRepository, HistoricoPedidoService historicoPedidoService, EntregaParcialService entregaParcialService) {
        this.entregadorRepository = entregadorRepository;
        this.entregaParcialRepository = entregaParcialRepository;
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoService = historicoPedidoService;
        this.entregaParcialService = entregaParcialService;
    }


    public Pedido confirmDriverArrivalByDeliveryMan (ConfirmarChegadaDTO confirmarChegadaDTO, String trackingCode, String cpf) {
        Entregador deliveryMan = entregadorRepository.findByCpf(cpf);

        if (deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }

        EntregaParcial partialDelivery = entregaParcialRepository.findByOrderTrackingCode(trackingCode).stream()
                .filter(delivery -> delivery.getDeliveryMan().getCpf().equals(cpf) && delivery.getDestinationCenter().getName().equals(confirmarChegadaDTO.getDestinationCenter()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrega parcial correspondente não encontrada para este entregador e centro de distribuição de destino. Verifique as informações e tente novamente."));


        CentroDistribuicao destinationCenter = partialDelivery.getDestinationCenter();

        if (destinationCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Destino não encontrado! Verifique o nome informado e tente novamente.");
        }
        CentroDistribuicao originCenter = partialDelivery.getOriginCenter();

        if (originCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Origem não encontrado! Verifique o nome informado e tente novamente.");
        }

        Pedido order = pedidoService.validationOnChangeStatus(destinationCenter.getEnterprise().getCnpj(), trackingCode, EM_TRANSITO);

        switch (destinationCenter.getCenterDistribuitionType()) {
            case TRANSACIONAL -> order.setStatus(EM_TRANSITO);
            case ULTIMA_MILHA -> order.setStatus(EM_DISTRIBUICAO);
        }

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, EM_TRANSITO, order.getStatus(), "Pedido chegou no centro de distribuição: " + confirmarChegadaDTO.getDestinationCenter());

        EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
        enviarPedidoDTO.setDeliveryManCpf(cpf);
        enviarPedidoDTO.setOriginCenter(originCenter.getName());
        enviarPedidoDTO.setDestinationCenter(destinationCenter.getName());
        entregaParcialService.registerPartialDeliveryByEnterprise(enviarPedidoDTO, order, destinationCenter.getEnterprise().getCnpj());

        return order;
    }


}
