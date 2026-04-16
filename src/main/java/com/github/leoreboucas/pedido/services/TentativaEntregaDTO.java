package com.github.leoreboucas.pedido.services;

import jakarta.validation.constraints.NotNull;

public record TentativaEntregaDTO(@NotNull EntregaFinalStatus status) {
}