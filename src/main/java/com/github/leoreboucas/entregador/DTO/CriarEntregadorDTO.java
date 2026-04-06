package com.github.leoreboucas.entregador.DTO;

import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.entregador.CategoriaCNH;
import com.github.leoreboucas.entregador.Disponibilidade;
import com.github.leoreboucas.entregador.TipoEntregador;
import com.github.leoreboucas.entregador.TipoVeiculo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriarEntregadorDTO {
    private String firstName;
    private String secondName;
    private String cpf;
    private String password;
    private LocalDateTime dateOfBirth;
    private String cellNumber;
    private String cep;
    private String street;
    private String complement;
    private String houseNumber;
    private String neighborhood;
    private String city;
    private String state;
    private CategoriaCNH cnhCategory;
    private TipoVeiculo vehicleType;
    private Disponibilidade availability;
    private double capacity;
    private TipoEntregador deliveryManType;
}
