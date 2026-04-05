package com.github.leoreboucas.empresa.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {
    private String name;
    private String cnpj;
    private LocalDateTime createdAt;
}
