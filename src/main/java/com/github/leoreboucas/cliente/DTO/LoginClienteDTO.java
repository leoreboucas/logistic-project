package com.github.leoreboucas.cliente.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginClienteDTO {
    @NotBlank
    @NotNull
    private String cpf;
    @NotBlank
    @NotNull
    private String password;
}
