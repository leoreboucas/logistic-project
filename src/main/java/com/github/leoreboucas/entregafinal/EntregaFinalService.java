package com.github.leoreboucas.entregafinal;
import com.github.leoreboucas.entregafinal.DTO.CriarPedidoFinalDTO;
import org.springframework.stereotype.Service;

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
