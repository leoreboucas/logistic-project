package com.github.leoreboucas.entregaparcial.DTO;

import com.github.leoreboucas.centrodistribuicao.CentroDistribuicao;
import com.github.leoreboucas.entregador.Entregador;
import com.github.leoreboucas.pedido.Pedido;

public record CriarEntregaParcialDTO(Pedido order, Entregador deliveryMan, CentroDistribuicao originCenter, CentroDistribuicao destinationCenter) {
}
