package com.github.leoreboucas.centrodistribuicao.DTO;

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
public class CriarCentroDistribuicaoDTO {
    @NotBlank
    @NotNull
    private String name;
    @NotBlank
    @NotNull
    private String cep;
    @NotBlank
    @NotNull
    private String street;
    @NotBlank
    @NotNull
    private String houseNumber;
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
