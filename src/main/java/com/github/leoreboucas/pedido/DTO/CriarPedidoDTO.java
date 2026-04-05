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
    @NotNull
    @NotBlank
    private String cpfCliente;
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
