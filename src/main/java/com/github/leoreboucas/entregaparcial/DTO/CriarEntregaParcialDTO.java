package com.github.leoreboucas.entregaparcial.DTO;

import com.github.leoreboucas.entregaparcial.StatusEntregaParcial;
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
public class CriarEntregaParcialDTO {
    @NotBlank
    @NotNull
    private String trackingCode;
    @NotBlank
    @NotNull
    private String cpfDeliveryMan;
    @NotBlank
    @NotNull
    private String originCenterName;
    @NotBlank
    @NotNull
    private String destinationCenterName;
    @NotNull
    private StatusEntregaParcial partialDeliveryStatus;
    @NotNull
    private LocalDateTime departureDate;
    @NotNull
    private LocalDateTime arrivalDate;
}
