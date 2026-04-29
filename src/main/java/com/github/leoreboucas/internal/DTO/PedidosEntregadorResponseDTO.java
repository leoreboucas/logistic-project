package com.github.leoreboucas.internal.DTO;

import java.util.List;

public record PedidosEntregadorResponseDTO(
        List<EntregaParcialResponseDTO> partialDelivery,
        List<EntregaFinalResponseDTO> finalDelivery
        ) {
}
