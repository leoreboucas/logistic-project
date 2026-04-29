package com.github.leoreboucas.internal.DTO;

import java.time.LocalDateTime;

public record EntregaFinalResponseDTO(
            String trackingCode,
            String originCenter,
            String customerCompleteName,
            String cep,
            String street,
            String houseNumber,
            String complement,
            String neighborhood,
            String city,
            String state,
            LocalDateTime departureDate,
            String observation
) {

}
