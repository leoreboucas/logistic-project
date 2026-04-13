package com.github.leoreboucas.centrodistribuicao.DTO;

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
    private String centerDistribuitionType;
    private String city;
    private String state;
}
