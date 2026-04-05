package com.github.leoreboucas.fornecedor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    Fornecedor findByCnpj(String cnpj);
}
