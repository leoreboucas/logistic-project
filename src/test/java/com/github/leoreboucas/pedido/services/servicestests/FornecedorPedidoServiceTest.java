package com.github.leoreboucas.pedido.services.servicestests;

import com.github.leoreboucas.fornecedor.Fornecedor;
import com.github.leoreboucas.fornecedor.FornecedorRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.CriarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import com.github.leoreboucas.pedido.services.FornecedorPedidoService;
import com.github.leoreboucas.pedido.services.PedidoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FornecedorPedidoServiceTest {
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private PedidoService pedidoService;
    @Mock
    private HistoricoPedidoService historicoPedidoService;

    @InjectMocks
    private FornecedorPedidoService fornecedorPedidoService;

    private final String cnpj = "cnpj";
    private final String trackingCode = "trackingCode";

    @Nested
    class CreateOrderBySupplier {
        @Test
        void notHaveSupplierOnCreateOrder() {
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(null);
            assertThrows(ResponseStatusException.class, () -> fornecedorPedidoService.createOrderBySupplier(new CriarPedidoDTO(), cnpj));
            verify(fornecedorRepository, times(1)).findByCnpj(cnpj);
        }

        @Test
        void sucessOnCreateOrder() {
            Fornecedor supplier = new Fornecedor();
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(supplier);
            when(pedidoService.generateTrackingCode()).thenReturn(trackingCode);

            CriarPedidoDTO criarPedidoDTO = new CriarPedidoDTO();
            criarPedidoDTO.setCustomerCompleteName("customer");

            Pedido result = fornecedorPedidoService.createOrderBySupplier(criarPedidoDTO, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.AGUARDANDO_POSTAGEM, result.getStatus());
            assertEquals(supplier, result.getFornecedor());
            assertEquals(trackingCode, result.getTrackingCode());

            verify(pedidoRepository, times(1)).save(any(Pedido.class));
            verify(fornecedorRepository, times(1)).findByCnpj(cnpj);
            verify(historicoPedidoService, times(1)).registerOrderHistory(any(), isNull(), eq(PedidoStatus.AGUARDANDO_POSTAGEM), isNull());
        }
    }

    @Nested
    class CancelOrderBySupplier {
        @Test
        void doNotHaveSupplierOnCancelOrder() {
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(null);
            assertThrows(ResponseStatusException.class, () -> fornecedorPedidoService.cancelOrderBySupplier(trackingCode, cnpj));
            verify(fornecedorRepository, times(1)).findByCnpj(cnpj);
        }

        @Test
        void doNotHaveOrderOnCancelOrder() {
            Fornecedor supplier = new Fornecedor();
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(supplier);

            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> fornecedorPedidoService.cancelOrderBySupplier(trackingCode, cnpj));

            verify(fornecedorRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void userDoNotHavePermissionToCancelOrder() {
            Fornecedor differentSupplier = new Fornecedor();
            differentSupplier.setCnpj("differentCnpj");
            when(fornecedorRepository.findByCnpj("differentCnpj")).thenReturn(differentSupplier);

            Pedido order = new Pedido();
            Fornecedor supplier = new Fornecedor();
            supplier.setCnpj(cnpj);
            order.setFornecedor(supplier);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            assertThrows(ResponseStatusException.class, () -> fornecedorPedidoService.cancelOrderBySupplier(trackingCode, "differentCnpj"));

            verify(fornecedorRepository).findByCnpj("differentCnpj");
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void doNotCanCancelOnActualStatus() {
            Fornecedor supplier = new Fornecedor();
            supplier.setCnpj(cnpj);
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(supplier);

            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            order.setFornecedor(supplier);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            assertThrows(ResponseStatusException.class, () -> fornecedorPedidoService.cancelOrderBySupplier(trackingCode, cnpj));

            verify(fornecedorRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void successOnCancelOrder() {
            Fornecedor supplier = new Fornecedor();
            supplier.setCnpj(cnpj);
            when(fornecedorRepository.findByCnpj(cnpj)).thenReturn(supplier);

            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
            order.setFornecedor(supplier);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            Pedido result = fornecedorPedidoService.cancelOrderBySupplier(trackingCode, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.CANCELADO, result.getStatus());

            verify(fornecedorRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(), eq(PedidoStatus.AGUARDANDO_POSTAGEM), eq(PedidoStatus.CANCELADO), eq("Pedido cancelado pelo fornecedor."));
        }
    }
}


