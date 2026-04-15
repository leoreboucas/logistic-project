package com.github.leoreboucas.pedido.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private String originCenter;
    @NotNull
    private String destinationCenter;
    private String actualStatus;
}
