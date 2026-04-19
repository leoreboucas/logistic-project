package com.github.leoreboucas.pedido.services.servicestests;

import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedido;
import com.github.leoreboucas.historicopedido.HistoricoPedidoRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import com.github.leoreboucas.pedido.services.PedidoService;
import com.github.leoreboucas.rastreamento.DTO.HistoricoItemDTO;
import com.github.leoreboucas.rastreamento.DTO.RastreamentoResponseDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private HistoricoPedidoRepository historicoPedidoRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    private String trackingCode = "trackingCode";
    private String cnpj = "cnpj";

    @InjectMocks
    private PedidoService pedidoService;

    @Nested
    class getOrdersHistoryByTrackingCode {
        @Test
        void orderNotFound () {
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> {
                pedidoService.getOrdersHistoryByTrackingCode(trackingCode);
            });

            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void successOnGetOrdersHistory () {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            HistoricoPedido orderHistory = new HistoricoPedido();
            orderHistory.setOrder(order);
            orderHistory.setPreviousStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
            orderHistory.setNewStatus(PedidoStatus.POSTADO);
            orderHistory.setObservation(null);
            orderHistory.setDateOfChange(LocalDateTime.now());

            List<HistoricoPedido> ordersHistory = List.of(orderHistory);
            when(historicoPedidoRepository.findByOrderTrackingCode(trackingCode)).thenReturn(ordersHistory);

            RastreamentoResponseDTO result = pedidoService.getOrdersHistoryByTrackingCode(trackingCode);

            assertNotNull(result);

            verify(pedidoRepository).findByTrackingCode(trackingCode);
            verify(historicoPedidoRepository).findByOrderTrackingCode(trackingCode);
        }
    }

    @Nested
    class ValidationOnChangeStatus {
        @Test
        void enterpriseNotFound() {
            when(empresaRepository.findByCnpj(cnpj)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> {
                pedidoService.validationOnChangeStatus(cnpj, trackingCode);
            });

            verify(empresaRepository).findByCnpj(cnpj);
        }

        @Test
        void orderNotFound() {
            Empresa enterprise = new Empresa();
            when(empresaRepository.findByCnpj(cnpj)).thenReturn(enterprise);

            Pedido order = new Pedido();
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> {
                pedidoService.validationOnChangeStatus(cnpj, trackingCode);
            });

            verify(empresaRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void cannotChangeStatusOnActualStatus () {
            Empresa enterprise = new Empresa();
            when(empresaRepository.findByCnpj(cnpj)).thenReturn(enterprise);

            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.ENTREGUE);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            assertThrows(ResponseStatusException.class, () -> {
                pedidoService.validationOnChangeStatus(cnpj, trackingCode);
            });

            verify(empresaRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }

        @Test
        void validToChangeStatus () {
            Empresa enterprise = new Empresa();
            when(empresaRepository.findByCnpj(cnpj)).thenReturn(enterprise);

            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);
            when(pedidoRepository.findByTrackingCode(trackingCode)).thenReturn(order);

            Pedido result = pedidoService.validationOnChangeStatus(cnpj, trackingCode);

            assertNotNull(result);

            verify(empresaRepository).findByCnpj(cnpj);
            verify(pedidoRepository).findByTrackingCode(trackingCode);
        }
    }
}
