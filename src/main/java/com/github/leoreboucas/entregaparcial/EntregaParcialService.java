package com.github.leoreboucas.entregaparcial;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregaparcial.DTO.CriarEntregaParcialDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntregaParcialService {
    private final EntregadorRepository entregadorRepository;
    private final EntregaParcialRepository entregaParcialRepository;

    public EntregaParcialService(EntregadorRepository entregadorRepository, EntregaParcialRepository entregaParcialRepository) {
        this.entregadorRepository = entregadorRepository;
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

    public List<EntregaParcial> getPartialDeliveriesByDeliveryMan(String cpf) {
        Entregador deliveryMan = entregadorRepository.findByCpf(cpf);

        if(deliveryMan == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entregador não encontrado");
        }

        return entregaParcialRepository.findByDeliveryMan(deliveryMan);
    }
}
