package com.github.leoreboucas.rastreamento;

import com.github.leoreboucas.pedido.DTO.PedidoResponseDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rastreamento")
public class RastreamentoController {
    PedidoService pedidoService;
    public RastreamentoController (PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{trackingCode}")
    public PedidoResponseDTO rastrear(@PathVariable String trackingCode) {
        Pedido pedido = pedidoService.findByTrackingCode(trackingCode);
        return new PedidoResponseDTO(pedido.getTrackingCode(), pedido.getStatus().toString());
    }
}