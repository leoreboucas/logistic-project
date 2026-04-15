package com.github.leoreboucas.pedido;

import com.github.leoreboucas.pedido.DTO.*;
import com.github.leoreboucas.pedido.services.EmpresaPedidoService;
import com.github.leoreboucas.pedido.services.EntregadorPedidoService;
import com.github.leoreboucas.pedido.services.FornecedorPedidoService;
import com.github.leoreboucas.pedido.services.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;
    private final FornecedorPedidoService fornecedorPedidoService;
    private final EmpresaPedidoService empresaPedidoService;
    private final EntregadorPedidoService entregadorPedidoService;

    public PedidoController(PedidoService pedidoService, FornecedorPedidoService fornecedorPedidoService, EmpresaPedidoService empresaPedidoService, EntregadorPedidoService entregadorPedidoService) {
        this.pedidoService = pedidoService;
        this.fornecedorPedidoService = fornecedorPedidoService;
        this.empresaPedidoService = empresaPedidoService;
        this.entregadorPedidoService = entregadorPedidoService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO createOrderBySupplierController (@RequestBody @Valid CriarPedidoDTO criarPedidoDTO) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Pedido order = fornecedorPedidoService.createOrderBySupplier(criarPedidoDTO, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @GetMapping
    public List<ListarPedidosDTO> getAllOrdersController () {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        return pedidoService.getAllOrdersByCnpj(cnpj);
    }

    @PatchMapping("/{trackingCode}/cancelar")
    public PedidoResponseDTO cancelOrderBySupplierController (@PathVariable String trackingCode) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = fornecedorPedidoService.cancelOrderBySupplier(trackingCode, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @PatchMapping("/{trackingCode}/confirmar-postagem")
    public PedidoResponseDTO confirmPostByEnterpriseController(@PathVariable String trackingCode) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = empresaPedidoService.confirmPostByEnterprise(trackingCode, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @PatchMapping("/{trackingCode}/confirmar-triagem")
    public PedidoResponseDTO confirmScreeningByEnterpriseController(@PathVariable String trackingCode) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = empresaPedidoService.confirmScreeningByEnterpise(trackingCode, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @PatchMapping("/{trackingCode}/confirmar-envio")
    @ResponseBody
    public PedidoResponseDTO confirmShippingByEnterpriseController(@RequestBody @Valid EnviarPedidoDTO enviarPedidoDTO, @PathVariable String trackingCode) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @PatchMapping("/{trackingCode}/confirmar-chegada")
    @ResponseBody
    public PedidoResponseDTO confirmDriverArrivalByDeliveryMan(@RequestBody @Valid ConfirmarChegadaDTO confirmarChegadaDTO, @PathVariable String trackingCode) {
        String cpf = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = entregadorPedidoService.confirmDriverArrivalByDeliveryMan(confirmarChegadaDTO, trackingCode, cpf);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }

    @PatchMapping("/{trackingCode}/saiu-para-entrega")
    @ResponseBody
    public PedidoResponseDTO outForDeliveryByEnterprise(@RequestBody @Valid EnviarPedidoDTO enviarPedidoDTO, @PathVariable String trackingCode) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Pedido order = empresaPedidoService.outForDeliveryByEnterprise(enviarPedidoDTO, trackingCode, cnpj);

        return new PedidoResponseDTO(order.getStatus().toString(), order.getTrackingCode());
    }
}
