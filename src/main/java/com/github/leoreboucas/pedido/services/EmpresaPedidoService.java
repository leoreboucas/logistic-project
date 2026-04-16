package com.github.leoreboucas.pedido.services;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.centrodistribuicao.CentroDistribuicaoRepository;
import com.github.leoreboucas.centrodistribuicao.TipoCentroDistribuicao;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.DTO.CriarPedidoFinalDTO;
import com.github.leoreboucas.entregafinal.EntregaFinalService;
import com.github.leoreboucas.entregaparcial.DTO.CriarEntregaParcialDTO;
import com.github.leoreboucas.entregaparcial.EntregaParcialService;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.EnviarEntregaFinalDTO;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.beans.factory.annotation.Value;
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
    private final EntregaFinalService entregaFinalService;
    private final EntregadorRepository entregadorRepository;
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    @Value("${delivery.max-attempts}")
    private int maxAttempts;


    public EmpresaPedidoService(PedidoService pedidoService, PedidoRepository pedidoRepository, HistoricoPedidoService historicoPedidoService, EntregaParcialService entregaParcialService, EntregaFinalService entregaFinalService, EntregadorRepository entregadorRepository, CentroDistribuicaoRepository centroDistribuicaoRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.historicoPedidoService = historicoPedidoService;
        this.entregaParcialService = entregaParcialService;
        this.entregaFinalService = entregaFinalService;
        this.entregadorRepository = entregadorRepository;
        this.centroDistribuicaoRepository = centroDistribuicaoRepository;
    }

    public Pedido confirmPostByEnterprise (String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode);

        if(order.getStatus() != AGUARDANDO_POSTAGEM) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        order.setStatus(PedidoStatus.POSTADO);

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, PedidoStatus.AGUARDANDO_POSTAGEM, PedidoStatus.POSTADO, "Postagem do pedido confirmada.");

        return order;
    }

    public Pedido confirmScreeningByEnterpise (String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode);

        if(order.getStatus() != POSTADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        order.setStatus(EM_TRIAGEM);

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, POSTADO, EM_TRIAGEM, "Triagem do pedido confirmada.");

        return order;
    }

    @Transactional
    public Pedido confirmShippingByEnterprise (EnviarPedidoDTO enviarPedidoDTO, String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode);

        PedidoStatus previousStatus = order.getStatus();

        CentroDistribuicao originCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);

        if (originCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Origem não encontrado! Verifique o nome informado e tente novamente.");
        }

        CentroDistribuicao destinationCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);

        if(destinationCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Destino não encontrado! Verifique o nome informado e tente novamente.");
        }

        switch (order.getStatus()) {
            case EM_TRIAGEM -> order.setStatus(EM_TRANSITO);
            case EM_TRANSITO -> {
                if (destinationCenter.getCenterDistribuitionType().equals(TipoCentroDistribuicao.TRANSACIONAL)) {
                    order.setStatus(EM_TRANSITO);
                } else {
                    order.setStatus(EM_DISTRIBUICAO);
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        Entregador deliveryMan = entregadorRepository.findByCpf(enviarPedidoDTO.getDeliveryManCpf());

        if (deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }

        pedidoRepository.save(order);

        historicoPedidoService.registerOrderHistory(order, previousStatus, order.getStatus(), "Pedido enviado para centro de distribuição: " + enviarPedidoDTO.getDestinationCenter());
        entregaParcialService.registerPartialDeliveryByEnterprise(new CriarEntregaParcialDTO(order, deliveryMan, originCenter, destinationCenter));

        return order;
    }

    public Pedido outForDeliveryByEnterprise(EnviarEntregaFinalDTO enviarEntregaFinalDTO, String trackingCode, String cnpj) {
        Pedido order = pedidoService.validationOnChangeStatus(cnpj, trackingCode);

        if(order.getStatus() != EM_DISTRIBUICAO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido não pode ser confirmado no status atual.");
        }

        order.setStatus(SAIU_PARA_ENTREGA);
        Entregador deliveryMan = entregadorRepository.findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf());

        if(deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }

        CentroDistribuicao originCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj);

        if(originCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Origem não encontrado! Verifique o nome informado e tente novamente.");
        }

        if(order.getDeliveryAttempts() == maxAttempts) {
            order.setStatus(DEVOLVIDO);
            historicoPedidoService.registerOrderHistory(order, EM_DISTRIBUICAO, DEVOLVIDO, "Número máximo de tentativas de entrega atingido. Pedido devolvido para o remetente.");
            pedidoRepository.save(order);
            return order;
        }

        pedidoRepository.save(order);
        historicoPedidoService.registerOrderHistory(order, EM_DISTRIBUICAO, SAIU_PARA_ENTREGA, "Pedido saiu para entrega. Entregador: " + deliveryMan.getFirstName() + " " + deliveryMan.getSecondName());
        entregaFinalService.registerFinalDeliveryByEnterprise(new CriarPedidoFinalDTO(order, deliveryMan, originCenter));

        return order;
    }
}
