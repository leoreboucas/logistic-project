package com.github.leoreboucas.centrodistribuicao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentroDistribuicaoRepository extends JpaRepository<CentroDistribuicao, Long> {
    List<CentroDistribuicao> findByEnterpriseCnpj(String cnpj);
    CentroDistribuicao findByNameAndEnterpriseCnpj(String name, String cnpj);
}
