package com.github.leoreboucas.pedido.services.servicestests;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.centrodistribuicao.CentroDistribuicaoRepository;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.entregador.EntregadorRepository;
import com.github.leoreboucas.entregafinal.DTO.CriarPedidoFinalDTO;
import com.github.leoreboucas.entregafinal.EntregaFinalService;
import com.github.leoreboucas.entregaparcial.EntregaParcialService;
import com.github.leoreboucas.historicopedido.HistoricoPedidoService;
import com.github.leoreboucas.pedido.DTO.EnviarEntregaFinalDTO;
import com.github.leoreboucas.pedido.DTO.EnviarPedidoDTO;
import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoRepository;
import com.github.leoreboucas.pedido.PedidoStatus;
import com.github.leoreboucas.pedido.services.EmpresaPedidoService;
import com.github.leoreboucas.pedido.services.PedidoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmpresaPedidoServiceTest {
    @Mock
    private PedidoService pedidoService;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private HistoricoPedidoService historicoPedidoService;
    @Mock
    private EntregaParcialService entregaParcialService;
    @Mock
    private EntregaFinalService entregaFinalService;
    @Mock
    private EntregadorRepository entregadorRepository;
    @Mock
    private CentroDistribuicaoRepository centroDistribuicaoRepository;

    private String cnpj = "cnpj";
    private String trackingCode = "trackingCode";

    @InjectMocks
    private EmpresaPedidoService empresaPedidoService;

    @Nested
    class confirmPostByEnterprise {
        @Test
        void cannotChangeConfirmPostOnThisActualStatus() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRANSITO);

            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmPostByEnterprise(trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
        }

        @Test
        void successOnConfirmPost() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.AGUARDANDO_POSTAGEM);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            Pedido result = empresaPedidoService.confirmPostByEnterprise(trackingCode, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.POSTADO, result.getStatus());

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(PedidoStatus.AGUARDANDO_POSTAGEM), eq(PedidoStatus.POSTADO), eq("Postagem do pedido confirmada."));
        }
    }

    @Nested
    class confirmScreeningByEnterprise {
        @Test
        void cannotChangeConfirmScreeningOnThisActualStatus() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRANSITO);

            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmScreeningByEnterpise(trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
        }

        @Test
        void successOnScreeningPost() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.POSTADO);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            Pedido result = empresaPedidoService.confirmScreeningByEnterpise(trackingCode, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.EM_TRIAGEM, result.getStatus());

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(PedidoStatus.POSTADO), eq(PedidoStatus.EM_TRIAGEM), eq("Triagem do pedido confirmada."));
        }
    }

    @Nested
    class confirmShippingByEnterprise {
        @Test
        void originCenterNotFound() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);

            EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
            enviarPedidoDTO.setOriginCenter("originCenter");

            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
        }

        @Test
        void destinationCenterNotFound() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
            enviarPedidoDTO.setOriginCenter("originCenter");
            enviarPedidoDTO.setDestinationCenter("destinationCenter");

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj)).thenReturn(originCenter);
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);
        }

        @Test
        void cannotChangeShippingOnActualStatus (){
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
            enviarPedidoDTO.setOriginCenter("originCenter");
            enviarPedidoDTO.setDestinationCenter("destinationCenter");

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj)).thenReturn(originCenter);
            CentroDistribuicao destinationCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj)).thenReturn(destinationCenter);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);
        }

        @Test
        void deliveryManNotFound () {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
            enviarPedidoDTO.setOriginCenter("originCenter");
            enviarPedidoDTO.setDestinationCenter("destinationCenter");
            enviarPedidoDTO.setDeliveryManCpf("deliveryManCpf");

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj)).thenReturn(originCenter);
            CentroDistribuicao destinationCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj)).thenReturn(destinationCenter);

            when(entregadorRepository.findByCpf(enviarPedidoDTO.getDeliveryManCpf())).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);
            verify(entregadorRepository).findByCpf(enviarPedidoDTO.getDeliveryManCpf());
        }
        @Test
        void successOnConfirmShipping() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarPedidoDTO enviarPedidoDTO = new EnviarPedidoDTO();
            enviarPedidoDTO.setOriginCenter("originCenter");
            enviarPedidoDTO.setDestinationCenter("destinationCenter");
            enviarPedidoDTO.setDeliveryManCpf("deliveryManCpf");

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj)).thenReturn(originCenter);
            CentroDistribuicao destinationCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj)).thenReturn(destinationCenter);

            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByCpf(enviarPedidoDTO.getDeliveryManCpf())).thenReturn(deliveryMan);

            Pedido result = empresaPedidoService.confirmShippingByEnterprise(enviarPedidoDTO, trackingCode, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.EM_TRANSITO, result.getStatus());

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getOriginCenter(), cnpj);
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarPedidoDTO.getDestinationCenter(), cnpj);
            verify(entregadorRepository).findByCpf(enviarPedidoDTO.getDeliveryManCpf());
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(PedidoStatus.EM_TRIAGEM), eq(PedidoStatus.EM_TRANSITO), eq("Pedido enviado para centro de distribuição: " + enviarPedidoDTO.getDestinationCenter()));
        }
    }
    @Nested
    class outForDeliveryByEnterprise {
        @Test
        void cannotChangeOutForDeliveryOnActualStatus() {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_TRIAGEM);

            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarEntregaFinalDTO enviarEntregaFinalDTO = new EnviarEntregaFinalDTO("deliveryManCpf", "originCenter");

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.outForDeliveryByEnterprise(enviarEntregaFinalDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
        }

        @Test
        void deliveryManNotFound () {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarEntregaFinalDTO enviarEntregaFinalDTO = new EnviarEntregaFinalDTO("deliveryManCpf", "originCenter");

            when(entregadorRepository.findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf())).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.outForDeliveryByEnterprise(enviarEntregaFinalDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(entregadorRepository).findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf());
        }

        @Test
        void originCenterNotFound () {
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarEntregaFinalDTO enviarEntregaFinalDTO = new EnviarEntregaFinalDTO("deliveryManCpf", "originCenter");

            Entregador deliveryMan = new Entregador();

            when(entregadorRepository.findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf())).thenReturn(deliveryMan);
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj)).thenReturn(null);

            assertThrows(ResponseStatusException.class, () -> empresaPedidoService.outForDeliveryByEnterprise(enviarEntregaFinalDTO, trackingCode, cnpj));

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(entregadorRepository).findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf());
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj);
        }

        @Test
        void orderReturnedDueToLimitedAttempts () {
            ReflectionTestUtils.setField(empresaPedidoService, "maxAttempts", 3);

            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            order.setDeliveryAttempts(4);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarEntregaFinalDTO enviarEntregaFinalDTO = new EnviarEntregaFinalDTO("deliveryManCpf", "originCenter");

            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf())).thenReturn(deliveryMan);

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj)).thenReturn(originCenter);

            Pedido result = empresaPedidoService.outForDeliveryByEnterprise(enviarEntregaFinalDTO, trackingCode, cnpj);

            assertNotNull(result);
            assertEquals(PedidoStatus.DEVOLVIDO, result.getStatus());

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(entregadorRepository).findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf());
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(PedidoStatus.EM_DISTRIBUICAO), eq(PedidoStatus.DEVOLVIDO), eq("Número máximo de tentativas de entrega atingido. Pedido devolvido para o remetente."));
        }

        @Test
        void sucessOnConfirmOutForDelivery () {
            ReflectionTestUtils.setField(empresaPedidoService, "maxAttempts", 3);
            Pedido order = new Pedido();
            order.setStatus(PedidoStatus.EM_DISTRIBUICAO);
            order.setDeliveryAttempts(2);
            when(pedidoService.validationOnChangeStatus(cnpj, trackingCode)).thenReturn(order);

            EnviarEntregaFinalDTO enviarEntregaFinalDTO = new EnviarEntregaFinalDTO("deliveryManCpf", "originCenter");

            Entregador deliveryMan = new Entregador();
            when(entregadorRepository.findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf())).thenReturn(deliveryMan);

            CentroDistribuicao originCenter = new CentroDistribuicao();
            when(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj)).thenReturn(originCenter);

            Pedido result = empresaPedidoService.outForDeliveryByEnterprise(enviarEntregaFinalDTO, trackingCode, cnpj);



            assertNotNull(result);
            assertEquals(PedidoStatus.SAIU_PARA_ENTREGA, result.getStatus());

            verify(pedidoService).validationOnChangeStatus(cnpj, trackingCode);
            verify(entregadorRepository).findByEnterpriseCnpjAndCpf(cnpj, enviarEntregaFinalDTO.deliveryManCpf());
            verify(centroDistribuicaoRepository).findByNameAndEnterpriseCnpj(enviarEntregaFinalDTO.originCenter(), cnpj);
            verify(pedidoRepository).save(order);
            verify(historicoPedidoService).registerOrderHistory(any(Pedido.class), eq(PedidoStatus.EM_DISTRIBUICAO), eq(PedidoStatus.SAIU_PARA_ENTREGA), eq("Pedido saiu para entrega. Entregador: " + deliveryMan.getFirstName() + " " + deliveryMan.getSecondName()));
            verify(entregaFinalService).registerFinalDeliveryByEnterprise(eq(new CriarPedidoFinalDTO(order, deliveryMan, originCenter)));
        }
    }

}
