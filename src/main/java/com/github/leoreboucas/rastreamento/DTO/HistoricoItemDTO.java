package com.github.leoreboucas.rastreamento.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoItemDTO {
    private String previousStatus;
    private String newStatus;
    private String observation;
    private LocalDateTime dateOfChange;
}
