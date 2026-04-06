package com.github.leoreboucas.entregador;

import com.github.leoreboucas.entregador.DTO.CriarEntregadorDTO;
import com.github.leoreboucas.entregador.DTO.EntregadorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/entregadores")
public class EntregadorController {
    private final EntregadorService entregadorService;

    public EntregadorController(EntregadorService entregadorService) {
        this.entregadorService = entregadorService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public EntregadorResponseDTO registerController(@RequestBody @Valid CriarEntregadorDTO criarEntregadorDTO) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Entregador deliveryMan = entregadorService.register(criarEntregadorDTO, cnpj);

        return new EntregadorResponseDTO(
                deliveryMan.getFirstName() + " " + deliveryMan.getSecondName(),
                deliveryMan.getCpf(),
                deliveryMan.getAvailability().toString(),
                deliveryMan.getCnhCategory().toString(),
                deliveryMan.getVehicleType().toString(),
                deliveryMan.getCreatedAt());
    }
}
