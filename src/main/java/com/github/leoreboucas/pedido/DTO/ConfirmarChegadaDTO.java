package com.github.leoreboucas.pedido.DTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ConfirmarChegadaDTO {
    @NotNull
    private String originCenter;
    @NotNull
    private String destinationCenter;
}
