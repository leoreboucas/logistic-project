package com.github.leoreboucas.pedido.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PedidoResponseDTO {
    private String status;
    private String trackingCode;
}
