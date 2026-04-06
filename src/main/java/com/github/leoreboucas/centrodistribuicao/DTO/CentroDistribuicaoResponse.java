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
public class CentroDistribuicaoResponse {
    private String name;
    private String cep;
    private String street;
    private String houseNumber;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
}
