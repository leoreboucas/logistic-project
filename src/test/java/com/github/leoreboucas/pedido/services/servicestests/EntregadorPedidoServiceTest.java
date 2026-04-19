package com.github.leoreboucas.pedido.services.servicestests;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.centrodistribuicao.TipoCentroDistribuicao;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.EntregaFinal;
import com.github.leoreboucas.entregafinal.EntregaFinalRepository;
import com.github.leoreboucas.entregaparcial.EntregaParcial;
import com.github.leoreboucas.entregaparcial.EntregaParcialRepository;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import com.github.leoreboucas.pedido.services.EntregaFinalStatus;
import com.github.leoreboucas.pedido.services.EntregadorPedidoService;
import com.github.leoreboucas.pedido.services.PedidoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static com.github.leoreboucas.pedido.PedidoStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntregadorPedidoServiceTest {
    @Mock
    private EntregadorRepository entregadorRepository;
    @Mock
    private EntregaParcialRepository entregaParcialRepository;
    @Mock
    private PedidoService pedidoService;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private HistoricoPedidoService historicoPedidoService;
    @Mock
    private EntregaFinalRepository entregaFinalRepository;

    private String cpf = "deliveryManCpf";
    private String trackingCode = "trackingCode";

    @InjectMocks
    private EntregadorPedidoService entregadorPedidoService;

    @Nested
    class ConfirmDriverArrivalByDeliveryMan {
        @Test
        void deliveryManNotFound () {
            when(entregadorRepository.findByCpf(cpf)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.confirmDriverArrivalByDeliveryMan(trackingCode, cpf));

            verify(entregadorRepository).findByCpf(cpf);
        }

        @Test
        void partialDeliveryNotFound () {
            Entregador deliveryMan = new Entregador();

            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);
            when(entregaParcialRepository.findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.confirmDriverArrivalByDeliveryMan(trackingCode, cpf));

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaParcialRepository).findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan);
        }

        @Test
        void confirmDriverArrivalOnTransactionalCenter () {
            Entregador deliveryMan = new Entregador();

            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);

            EntregaParcial partialDelivery = new EntregaParcial();
            CentroDistribuicao destinationCenter = new CentroDistribuicao();
            destinationCenter.setCenterDistribuitionType(TipoCentroDistribuicao.TRANSACIONAL);
            partialDelivery.setDestinationCenter(destinationCenter);
            Empresa enterprise = new Empresa();
            enterprise.setCnpj("cnpj");
            destinationCenter.setEnterprise(enterprise);
            when(entregaParcialRepository.findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan)).thenReturn(partialDelivery);

            Pedido order = new Pedido();
            when(pedidoService.validationOnChangeStatus(partialDelivery.getDestinationCenter().getEnterprise().getCnpj(),trackingCode)).thenReturn(order);

            Pedido result = entregadorPedidoService.confirmDriverArrivalByDeliveryMan(trackingCode, cpf);

            assertNotNull(result);
            assertEquals(PedidoStatus.EM_TRANSITO, result.getStatus());

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaParcialRepository).findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan);
            verify(pedidoService).validationOnChangeStatus(partialDelivery.getDestinationCenter().getEnterprise().getCnpj(),trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(EM_TRANSITO), eq(EM_TRANSITO), eq("Pedido chegou no centro de distribuição: " + partialDelivery.getDestinationCenter().getName()));

        }

        @Test
        void confirmDriverArrivalOnLastMileCenter () {
            Entregador deliveryMan = new Entregador();

            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);

            EntregaParcial partialDelivery = new EntregaParcial();
            CentroDistribuicao destinationCenter = new CentroDistribuicao();
            destinationCenter.setCenterDistribuitionType(TipoCentroDistribuicao.ULTIMA_MILHA);
            partialDelivery.setDestinationCenter(destinationCenter);
            Empresa enterprise = new Empresa();
            enterprise.setCnpj("cnpj");
            destinationCenter.setEnterprise(enterprise);
            when(entregaParcialRepository.findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan)).thenReturn(partialDelivery);

            Pedido order = new Pedido();
            when(pedidoService.validationOnChangeStatus(partialDelivery.getDestinationCenter().getEnterprise().getCnpj(),trackingCode)).thenReturn(order);

            Pedido result = entregadorPedidoService.confirmDriverArrivalByDeliveryMan(trackingCode, cpf);

            assertNotNull(result);
            assertEquals(PedidoStatus.EM_DISTRIBUICAO, result.getStatus());

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaParcialRepository).findLatestByOrderTrackingCodeAndDeliveryMan(trackingCode, deliveryMan);
            verify(pedidoService).validationOnChangeStatus(partialDelivery.getDestinationCenter().getEnterprise().getCnpj(),trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(EM_TRANSITO), eq(EM_DISTRIBUICAO), eq("Pedido chegou no centro de distribuição: " + partialDelivery.getDestinationCenter().getName()));

        }
    }

    @Nested
    class RegisterAttemptByDeliveryMan {
        @Test
        void deliveryManNotFound () {
            when(entregadorRepository.findByCpf(cpf)).thenReturn(null);;
            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.FRACASSO, trackingCode, cpf));

            verify(entregadorRepository).findByCpf(cpf);
        }

        @Test
        void finalDeliveryNotFound () {
            Entregador deliveryMan = new Entregador();

            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);
            when(entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.FRACASSO, trackingCode, cpf));

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaFinalRepository).findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);
        }

        @Test
        void deliveryManNotAuthorized () {
            Entregador differentDeliveryMan = new Entregador();
            when(entregadorRepository.findByCpf("differentCpf")).thenReturn(differentDeliveryMan);

            Entregador deliveryMan = new Entregador();
            EntregaFinal finalDelivery = new EntregaFinal();
            finalDelivery.setDeliveryMan(deliveryMan);
            when(entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode)).thenReturn(finalDelivery);

            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.FRACASSO, trackingCode, "differentCpf"));

            verify(entregadorRepository).findByCpf("differentCpf");
            verify(entregaFinalRepository).findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);
        }

        @Test
        void cannotChangeOrderOnActualStatus () {
            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);

            EntregaFinal finalDelivery = new EntregaFinal();
            CentroDistribuicao originCenter = new CentroDistribuicao();
            Empresa enterprise = new Empresa();
            enterprise.setCnpj("cnpj");
            originCenter.setEnterprise(enterprise);
            finalDelivery.setOriginCenter(originCenter);
            finalDelivery.setDeliveryMan(deliveryMan);
            when(entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode)).thenReturn(finalDelivery);
            Pedido order = new Pedido();
            order.setStatus(EM_TRANSITO);


            when(pedidoService.validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode)).thenReturn(order);
            assertThrows(ResponseStatusException.class, () -> entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.FRACASSO, trackingCode, cpf));

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaFinalRepository).findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);
            verify(pedidoService).validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode);
        }

        @Test
        void registerAttemptAsFailure () {
            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);

            EntregaFinal finalDelivery = new EntregaFinal();
            CentroDistribuicao originCenter = new CentroDistribuicao();
            Empresa enterprise = new Empresa();
            enterprise.setCnpj("cnpj");
            originCenter.setEnterprise(enterprise);
            finalDelivery.setOriginCenter(originCenter);
            finalDelivery.setDeliveryMan(deliveryMan);
            when(entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode)).thenReturn(finalDelivery);
            Pedido order = new Pedido();
            order.setStatus(SAIU_PARA_ENTREGA);

            when(pedidoService.validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode)).thenReturn(order);

            Pedido result = entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.FRACASSO, trackingCode, cpf);

            assertNotNull(result);
            assertEquals(1, result.getDeliveryAttempts());
            assertEquals(EM_DISTRIBUICAO, result.getStatus());

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaFinalRepository).findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);
            verify(pedidoService).validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(SAIU_PARA_ENTREGA), eq(EM_DISTRIBUICAO), eq("Entrega do pedido falhou! Pedido retornou para Centro de distribuição."));
        }

        @Test
        void registerAttemptAsSuccess () {
            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByCpf(cpf)).thenReturn(deliveryMan);

            EntregaFinal finalDelivery = new EntregaFinal();
            CentroDistribuicao originCenter = new CentroDistribuicao();
            Empresa enterprise = new Empresa();
            enterprise.setCnpj("cnpj");
            originCenter.setEnterprise(enterprise);
            finalDelivery.setOriginCenter(originCenter);
            finalDelivery.setDeliveryMan(deliveryMan);
            when(entregaFinalRepository.findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode)).thenReturn(finalDelivery);
            Pedido order = new Pedido();
            order.setStatus(SAIU_PARA_ENTREGA);

            when(pedidoService.validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode)).thenReturn(order);

            Pedido result = entregadorPedidoService.registerAttemptByDeliveryMan(EntregaFinalStatus.SUCESSO, trackingCode, cpf);

            assertNotNull(result);
            assertEquals(1, result.getDeliveryAttempts());
            assertEquals(ENTREGUE, result.getStatus());

            verify(entregadorRepository).findByCpf(cpf);
            verify(entregaFinalRepository).findTopByOrderTrackingCodeOrderByCreatedAtDesc(trackingCode);
            verify(pedidoService).validationOnChangeStatus(finalDelivery.getOriginCenter().getEnterprise().getCnpj(), trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(SAIU_PARA_ENTREGA), eq(ENTREGUE), eq("Pedido entregue com sucesso"));
        }
    }
}
