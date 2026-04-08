package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.centrodistribuicao.CentroDistribuicaoRepository;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregaparcial.DTO.CriarEntregaParcialDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoService;
import com.github.leoreboucas.pedido.PedidoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.github.leoreboucas.pedido.PedidoStatus.*;

@Service
public class EntregaParcialService {
    private final EntregaParcialRepository entregaParcialRepository;
    private final EmpresaRepository empresaRepository;
    private final PedidoRepository pedidoRepository;
    private final EntregadorRepository entregadorRepository;
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    private final PedidoService pedidoService;

    public EntregaParcialService(EntregaParcialRepository entregaParcialRepository, EmpresaRepository empresaRepository, PedidoRepository pedidoRepository, EntregadorRepository entregadorRepository, CentroDistribuicaoRepository centroDistribuicaoRepository, PedidoService pedidoService) {
        this.entregaParcialRepository = entregaParcialRepository;
        this.empresaRepository = empresaRepository;
        this.pedidoRepository = pedidoRepository;
        this.entregadorRepository = entregadorRepository;
        this.centroDistribuicaoRepository = centroDistribuicaoRepository;
        this.pedidoService = pedidoService;
    }

    public EntregaParcial register(CriarEntregaParcialDTO criarEntregaParcialDTO, String cnpj) {
        Empresa enterprise = empresaRepository.findByCnpj(cnpj);

        if(enterprise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }

        Pedido order = pedidoRepository.findByTrackingCode(criarEntregaParcialDTO.getTrackingCode());

        if(order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }

        Entregador deliveryMan = entregadorRepository.findByCpf(criarEntregaParcialDTO.getCpfDeliveryMan());

        if(deliveryMan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entregador não encontrado");
        }

        CentroDistribuicao originCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(
                criarEntregaParcialDTO.getOriginCenterName(), cnpj
        );

        if(originCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de distribuição de origem não encontrado");
        }

        CentroDistribuicao destinationCenter = centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(
                criarEntregaParcialDTO.getDestinationCenterName(), cnpj
        );

        if(destinationCenter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de distribuição de destino não encontrado");
        }

        StatusEntregaParcial expectStatus = verifyIfStatusIsValid(order.getStatus());

        if (expectStatus == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é possível criar entrega parcial para o status atual do pedido.");
        }


        if (!criarEntregaParcialDTO.getPartialDeliveryStatus().equals(expectStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status da entrega parcial incompatível com o status atual do pedido.");
        }



        EntregaParcial newPartialDelivery = new EntregaParcial();
        newPartialDelivery.setOrder(order);
        newPartialDelivery.setDeliveryMan(deliveryMan);
        newPartialDelivery.setOriginCenter(originCenter);
        newPartialDelivery.setDestinationCenter(destinationCenter);
        newPartialDelivery.setPartialDeliveryStatus(criarEntregaParcialDTO.getPartialDeliveryStatus());
        newPartialDelivery.setDepartureDate(criarEntregaParcialDTO.getDepartureDate());
        newPartialDelivery.setArrivalDate(criarEntregaParcialDTO.getArrivalDate());

        pedidoService.updateStatusByPartialDelivery(newPartialDelivery);
        entregaParcialRepository.save(newPartialDelivery);

        return newPartialDelivery;

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
