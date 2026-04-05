package com.github.leoreboucas.fornecedor.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginFornecedorDTO {
    @NotBlank
    private String cnpj;
    @NotBlank
    private String password;

}
