package com.github.leoreboucas.entregaparcial.DTO;

import com.github.leoreboucas.entregaparcial.EntregaParcial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListarEntregasParciaisDTO {
    private String trackingCode;
    private String originCenter;
    private String destinationCenter;

    public static List<ListarEntregasParciaisDTO> toList(List<EntregaParcial> allPartialDelivery) {
        return allPartialDelivery.stream().map(partialDelivery -> new ListarEntregasParciaisDTO(
                partialDelivery.getOrder().getTrackingCode(),
                partialDelivery.getOriginCenter().getName(),
                partialDelivery.getDestinationCenter().getName()
        )).toList();
    }
}
