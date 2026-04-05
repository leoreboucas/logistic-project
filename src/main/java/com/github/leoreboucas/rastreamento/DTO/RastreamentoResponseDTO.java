package com.github.leoreboucas.rastreamento.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RastreamentoResponseDTO {
    private String status;
    private String trackingCode;
    private List<HistoricoItemDTO> ordersHistory;
}
