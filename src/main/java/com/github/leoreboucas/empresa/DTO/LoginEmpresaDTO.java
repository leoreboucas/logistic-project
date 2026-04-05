package com.github.leoreboucas.empresa.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginEmpresaDTO {
    @NotBlank
    @NotNull
    private String cnpj;
    @NotBlank
    @NotNull
    private String password;
}
