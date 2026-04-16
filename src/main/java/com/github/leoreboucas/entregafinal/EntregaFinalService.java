package com.github.leoreboucas.entregafinal;

import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.DTO.CriarPedidoFinalDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class EntregaFinalService {
    private final EntregaFinalRepository entregaFinalRepository;

    public EntregaFinalService(EntregaFinalRepository entregaFinalRepository) {

        this.entregaFinalRepository = entregaFinalRepository;
    }

    public void registerFinalDeliveryByEnterprise (CriarPedidoFinalDTO criarPedidoFinalDTO) {
        EntregaFinal finalDelivery = new EntregaFinal();
        finalDelivery.setDeliveryMan(criarPedidoFinalDTO.deliveryMan());
        finalDelivery.setOrder(criarPedidoFinalDTO.order());
        finalDelivery.setOriginCenter(criarPedidoFinalDTO.originCenter());
        finalDelivery.setDepartureDate(LocalDateTime.now());

        entregaFinalRepository.save(finalDelivery);
    }
}
