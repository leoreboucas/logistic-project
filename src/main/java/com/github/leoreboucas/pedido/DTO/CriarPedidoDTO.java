package com.github.leoreboucas.pedido.DTO;
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
public class CriarPedidoDTO {
    @NotBlank
    private String customerCompleteName;
    @NotBlank
    private String cellNumber;
    @NotBlank
    private String cep;
    @NotBlank
    private String street;
    @NotBlank
    private String houseNumber;
    private String complement;
    @NotBlank
    private String neighborhood;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotNull
    private double weight;
    @NotNull
    private double length;
    @NotNull
    private double width;
    @NotNull
    private double depth;
    private String observation;
}
