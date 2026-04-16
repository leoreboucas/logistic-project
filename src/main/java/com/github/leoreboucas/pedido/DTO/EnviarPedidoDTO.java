package com.github.leoreboucas.pedido.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnviarPedidoDTO {
    @NotBlank
    private String deliveryManCpf;
    @NotBlank
    private String originCenter;
    @NotBlank
    private String destinationCenter;
}
