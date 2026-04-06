package com.github.leoreboucas.entregador.DTO;

import com.github.leoreboucas.entregador.CategoriaCNH;
import com.github.leoreboucas.entregador.Disponibilidade;
import com.github.leoreboucas.entregador.TipoEntregador;
import com.github.leoreboucas.entregador.TipoVeiculo;
import jakarta.validation.constraints.Email;
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
public class CriarEntregadorDTO {
    @NotBlank
    @NotNull
    private String firstName;
    @NotBlank
    @NotNull
    private String secondName;
    @NotBlank
    @NotNull
    private String cpf;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @NotNull
    private String password;
    @NotBlank
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
    private String complement;
    @NotBlank
    @NotNull
    private String houseNumber;
    @NotBlank
    @NotNull
    private String neighborhood;
    @NotBlank
    @NotNull
    private String city;
    @NotBlank
    @NotNull
    private String state;
    @NotBlank
    @NotNull
    private CategoriaCNH cnhCategory;
    @NotBlank
    @NotNull
    private TipoVeiculo vehicleType;
    @NotBlank
    @NotNull
    private Disponibilidade availability;
    @NotBlank
    @NotNull
    private double capacity;
    @NotBlank
    @NotNull
    private TipoEntregador deliveryManType;
}
