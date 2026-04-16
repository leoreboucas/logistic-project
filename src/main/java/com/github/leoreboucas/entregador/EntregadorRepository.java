package com.github.leoreboucas.entregador;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    Entregador findByCpf(@NotBlank String cpf);
    Entregador findByEnterpriseCnpjAndCpf(@NotBlank String cnpj, @NotBlank String cpf);
}
