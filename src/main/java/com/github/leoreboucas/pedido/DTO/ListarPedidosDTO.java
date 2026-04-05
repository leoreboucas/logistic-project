package com.github.leoreboucas.pedido.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ListarPedidosDTO {
    private String trackingCode;
    private String status;
    private LocalDateTime forecastDelivery;
    private String clientName;
}
