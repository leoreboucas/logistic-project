package com.github.leoreboucas.entregaparcial;
import com.github.leoreboucas.entregaparcial.DTO.CriarEntregaParcialDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EntregaParcialService {
    private final EntregaParcialRepository entregaParcialRepository;

    public EntregaParcialService(EntregaParcialRepository entregaParcialRepository) {
        this.entregaParcialRepository = entregaParcialRepository;
    }

    public void registerPartialDeliveryByEnterprise (CriarEntregaParcialDTO criarEntregaParcialDTO) {

        EntregaParcial partialDelivery = new EntregaParcial();
        partialDelivery.setDeliveryMan(criarEntregaParcialDTO.deliveryMan());
        partialDelivery.setOriginCenter(criarEntregaParcialDTO.originCenter());
        partialDelivery.setDestinationCenter(criarEntregaParcialDTO.destinationCenter());
        partialDelivery.setOrder(criarEntregaParcialDTO.order());
        partialDelivery.setDepartureDate(LocalDateTime.now());

        entregaParcialRepository.save(partialDelivery);
    }

}
