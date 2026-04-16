package com.github.leoreboucas.entregafinal.DTO;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.pedido.Pedido;

public record CriarPedidoFinalDTO(Pedido order, Entregador deliveryMan, CentroDistribuicao originCenter) {
}
