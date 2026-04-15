package com.github.leoreboucas.rastreamento;

import com.github.leoreboucas.pedido.services.PedidoService;
import com.github.leoreboucas.rastreamento.DTO.RastreamentoResponseDTO;
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
    public RastreamentoResponseDTO trackController(@PathVariable String trackingCode) {
        return pedidoService.getOrdersHistoryByTrackingCode(trackingCode);
    }
}