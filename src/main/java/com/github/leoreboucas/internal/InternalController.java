package com.github.leoreboucas.internal;

import com.github.leoreboucas.internal.DTO.*;
import com.github.leoreboucas.pedido.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/pedidos")
@RequiredArgsConstructor
public class InternalController {
    private final InternalService internalService;

    @GetMapping(params = "customerCpf")
    public List<PedidoClienteDTO> getOrdersByCustomerCpf(@RequestParam(required = false) String customerCpf) {
        List<Pedido> orders = internalService.getOrders(customerCpf);
        return orders.stream().map(order -> new PedidoClienteDTO(
                        order.getTrackingCode(),
                        order.getStatus().toString(),
                        order.getCep(),
                        order.getStreet(),
                        order.getHouseNumber(),
                        order.getComplement(),
                        order.getNeighborhood(),
                        order.getCity(),
                        order.getState(),
                        order.getObservation()
                )).toList();
    }

    @GetMapping(params = "deliveryManCpf")
    PedidosEntregadorResponseDTO getOrdersByDeliveryManCpf(@RequestParam(required = false) String deliveryManCpf) {
        PedidosEntregadorDTO orders = internalService.getDeliveryManOrders(deliveryManCpf);
        return new PedidosEntregadorResponseDTO(
                orders.partialDelivery().stream().map(partial -> new EntregaParcialResponseDTO(
                        partial.getOrder().getTrackingCode(),
                        partial.getOrder().getObservation(),
                        partial.getDepartureDate(),
                        partial.getOriginCenter().getName(),
                        partial.getDestinationCenter().getName()
                )).toList(),
                orders.finalDelivery().stream().map(finalDelivery -> new EntregaFinalResponseDTO(
                        finalDelivery.getOrder().getTrackingCode(),
                        finalDelivery.getOriginCenter().getName(),
                        finalDelivery.getOrder().getCustomerCompleteName(),
                        finalDelivery.getOrder().getCep(),
                        finalDelivery.getOrder().getStreet(),
                        finalDelivery.getOrder().getHouseNumber(),
                        finalDelivery.getOrder().getComplement(),
                        finalDelivery.getOrder().getNeighborhood(),
                        finalDelivery.getOrder().getCity(),
                        finalDelivery.getOrder().getState(),
                        finalDelivery.getDepartureDate(),
                        finalDelivery.getOrder().getObservation()
                )).toList()
        );

    }
}
