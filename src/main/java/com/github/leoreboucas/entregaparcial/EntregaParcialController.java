package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.entregaparcial.DTO.CriarEntregaParcialDTO;
import com.github.leoreboucas.entregaparcial.DTO.EntregaParcialResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/entregas-parciais")
public class EntregaParcialController {
    private final EntregaParcialService entregaParcialService;

    public EntregaParcialController(EntregaParcialService entregaParcialService) {
        this.entregaParcialService = entregaParcialService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public EntregaParcialResponseDTO registerController(@RequestBody @Valid CriarEntregaParcialDTO criarEntregaParcialDTO) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        EntregaParcial entregaParcial = entregaParcialService.register(criarEntregaParcialDTO, cnpj);
        return new EntregaParcialResponseDTO(
                entregaParcial.getId(),
                entregaParcial.getOrder().getTrackingCode(),
                entregaParcial.getDeliveryMan().getFirstName() + " " + entregaParcial.getDeliveryMan().getSecondName(),
                entregaParcial.getPartialDeliveryStatus().toString(),
                entregaParcial.getDepartureDate(),
                entregaParcial.getArrivalDate()
        );

    }
}
