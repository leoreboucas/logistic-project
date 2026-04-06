package com.github.leoreboucas.entregador.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntregadorResponseDTO {
    private String name;
    private String cpf;
    private String disponibilidade;
    private String categoriaCNH;
    private String tipoVeiculo;
    private LocalDateTime createdAt;

}
