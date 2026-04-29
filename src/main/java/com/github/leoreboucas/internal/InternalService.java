package com.github.leoreboucas.internal;

import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.EntregaFinal;
import com.github.leoreboucas.entregafinal.EntregaFinalRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.entregaparcial.EntregaParcialRepository;
import com.github.leoreboucas.internal.DTO.PedidosEntregadorDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InternalService {
    private final PedidoRepository pedidoRepository;
    private final EntregadorRepository entregadorRepository;
    private final EntregaParcialRepository entregaParcialRepository;
    private final EntregaFinalRepository entregaFinalRepository;

    List<Pedido> getOrders(String customerCpf) {
        if(customerCpf.isEmpty()) {
            throw new IllegalArgumentException("CPF do cliente é obrigatório.");
        }

        return pedidoRepository.findByCustomerCpf(customerCpf);
    }

    PedidosEntregadorDTO getDeliveryManOrders(String deliveryManCpf) {
        if(deliveryManCpf.isEmpty()) {
            throw new IllegalArgumentException("CPF do entregador é obrigatório.");
        }

        Optional<Entregador> deliveryMan = Optional.ofNullable(entregadorRepository.findByCpf(deliveryManCpf));
        if(deliveryMan.isEmpty()) {
            throw new IllegalArgumentException("Entregador não encontrado.");
        }

        List<EntregaParcial> partialDeliveries = entregaParcialRepository.findByDeliveryMan(deliveryMan.get());
        List<EntregaFinal> finalDeliveries = entregaFinalRepository.findByDeliveryMan(deliveryMan.get());

        return new PedidosEntregadorDTO(partialDeliveries, finalDeliveries);
    }
}
