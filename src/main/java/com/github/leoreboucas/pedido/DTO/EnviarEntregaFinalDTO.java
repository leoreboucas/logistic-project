package com.github.leoreboucas.pedido.DTO;

import jakarta.validation.constraints.NotBlank;

public record EnviarEntregaFinalDTO(@NotBlank String deliveryManCpf, @NotBlank String originCenter) {
}
