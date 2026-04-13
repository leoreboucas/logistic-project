package com.github.leoreboucas.entregaparcial.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntregaParcialResponseDTO {
    private long id;
    private String trackingCode;
    private String partialDeliveryStatus;
    private LocalDateTime departureDate;
    private LocalDateTime arrivalDate;
}
