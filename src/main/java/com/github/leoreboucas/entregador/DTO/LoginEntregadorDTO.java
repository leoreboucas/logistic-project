package com.github.leoreboucas.entregador.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LoginEntregadorDTO {
    @NotBlank
    @NotNull
    private String cpf;
    @NotBlank
    @NotNull
    private String password;
}
