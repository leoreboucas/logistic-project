package com.github.leoreboucas.entregador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    Entregador findByCpf(@NotBlank @NotNull String cpf);
}
