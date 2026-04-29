package com.github.leoreboucas.internal.DTO;

public record PedidoClienteDTO(
        String trackingCode,
        String status,
        String cep,
        String street,
        String houseNumber,
        String complement,
        String neighborhood,
        String city,
        String state,
        String observation
) {
}
