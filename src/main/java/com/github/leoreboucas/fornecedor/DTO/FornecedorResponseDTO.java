package com.github.leoreboucas.fornecedor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FornecedorResponseDTO {
    private String name;
    private String cnpj;
}
