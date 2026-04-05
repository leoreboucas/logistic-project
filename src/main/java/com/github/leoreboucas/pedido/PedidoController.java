package com.github.leoreboucas.pedido;

import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.DTO.PedidoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO createOrderBySupplierController (@RequestBody @Valid CriarPedidoDTO criarPedidoDTO) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Pedido order = pedidoService.createOrderBySupplier(criarPedidoDTO, cnpj);

        return new PedidoResponseDTO(order.getStatus(), order.getTrackingCode());
    }
}
