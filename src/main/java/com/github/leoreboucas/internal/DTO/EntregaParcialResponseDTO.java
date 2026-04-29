package com.github.leoreboucas.internal.DTO;

import java.time.LocalDateTime;

public record EntregaParcialResponseDTO(
        String trackingCode,
        String observation,
        LocalDateTime departureDate,
        String originCenter,
        String destinationCenter
) {
}
