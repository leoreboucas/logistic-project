package com.github.leoreboucas.cliente.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriarClienteDTO {
    @NotBlank
    @NotNull
    private String firstName;
    @NotBlank
    @NotNull
    private String secondName;
    @NotBlank
    @NotNull
    private String password;
    @NotBlank
    @NotNull
    private String cpf;
    @NotNull
    private LocalDateTime dateOfBirth;
    @NotBlank
    @NotNull
    private String cellNumber;
    @NotBlank
    @NotNull
    private String cep;
    @NotBlank
    @NotNull
    private String street;
    @NotBlank
    @NotNull
    private String houseNumber;
    @NotBlank
    private String complement;
    @NotBlank
    @NotNull
    private String neighborhood;
    @NotBlank
    @NotNull
    private String city;
    @NotBlank
    @NotNull
    private String state;
}
