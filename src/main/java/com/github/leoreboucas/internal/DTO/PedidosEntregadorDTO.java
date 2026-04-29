package com.github.leoreboucas.internal.DTO;

import com.github.leoreboucas.entregafinal.EntregaFinal;
import com.github.leoreboucas.entregaparcial.EntregaParcial;

import java.util.List;

public record PedidosEntregadorDTO(
        List<EntregaParcial> partialDelivery,
        List<EntregaFinal> finalDelivery
) {
}
