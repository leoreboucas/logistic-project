package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.centrodistribuicao.CentroDistribuicaoRepository;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

import static com.github.leoreboucas.pedido.PedidoStatus.*;

@Service
public class EntregaParcialService {
    private final EntregaParcialRepository entregaParcialRepository;
    private final EntregadorRepository entregadorRepository;
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;

    public EntregaParcialService(EntregaParcialRepository entregaParcialRepository, EntregadorRepository entregadorRepository, CentroDistribuicaoRepository centroDistribuicaoRepository) {
        this.entregaParcialRepository = entregaParcialRepository;
        this.entregadorRepository = entregadorRepository;
        this.centroDistribuicaoRepository = centroDistribuicaoRepository;
    }

    public void registerPartialDeliveryByEnterprise (EnviarPedidoDTO enviarPedidoDTO, Pedido order, String cnpj) {
        Entregador deliveryMan = entregadorRepository.findByCpf(enviarPedidoDTO.getDeliveryManCpf());
        if (deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado! Verifique o CPF informado e tente novamente.");
        }
        CentroDistribuicao originCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
        if (originCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Origem não encontrado! Verifique o nome informado e tente novamente.");
        }
        CentroDistribuicao destinationCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);
        if (destinationCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de Distribuição de Destino não encontrado! Verifique o nome informado e tente novamente.");
        }


        EntregaParcial partialDelivery = new EntregaParcial();
        partialDelivery.setDeliveryMan(deliveryMan);
        partialDelivery.setOriginCenter(originCenter);
        partialDelivery.setDestinationCenter(destinationCenter);
        partialDelivery.setOrder(order);
        partialDelivery.setPartialDeliveryStatus(verifyIfStatusIsValid(order.getStatus()));
        partialDelivery.setDepartureDate(LocalDateTime.now());

        entregaParcialRepository.save(partialDelivery);
    }

    private StatusEntregaParcial verifyIfStatusIsValid(PedidoStatus statusOrder) {
        Map<PedidoStatus, StatusEntregaParcial> validTransitions = Map.of(
                AGUARDANDO_POSTAGEM, StatusEntregaParcial.AGUARDANDO_POSTAGEM,
                POSTADO, StatusEntregaParcial.POSTADO,
                EM_TRIAGEM, StatusEntregaParcial.EM_TRIAGEM,
                EM_TRANSITO, StatusEntregaParcial.EM_TRANSITO,
                EM_DISTRIBUICAO, StatusEntregaParcial.EM_DISTRIBUICAO,
                SAIU_PARA_ENTREGA, StatusEntregaParcial.SAIU_PARA_ENTREGA
        );

        return validTransitions.get(statusOrder);
    }
}
