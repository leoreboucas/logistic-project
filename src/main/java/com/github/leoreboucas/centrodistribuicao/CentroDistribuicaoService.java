package com.github.leoreboucas.centrodistribuicao;

import com.github.leoreboucas.centrodistribuicao.DTO.CriarCentroDistribuicaoDTO;
import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CentroDistribuicaoService {
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    private final EmpresaRepository empresaRepository;

    public CentroDistribuicaoService(CentroDistribuicaoRepository centroDistribuicaoRepository, EmpresaRepository empresaRepository) {
        this.centroDistribuicaoRepository = centroDistribuicaoRepository;
        this.empresaRepository = empresaRepository;
    }

    public CentroDistribuicao register (CriarCentroDistribuicaoDTO criarCentroDistribuicaoDTO, String cnpj) {
        Optional<CentroDistribuicao> distribuitionCenter = Optional.ofNullable(centroDistribuicaoRepository.findByNameAndEnterpriseCnpj(
                criarCentroDistribuicaoDTO.getName(), cnpj
        ));
        Empresa enterprise = empresaRepository.findByCnpj(cnpj);

        if(enterprise == null) {
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.");
        }

        if(distribuitionCenter.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um centro de distribuição registrado com esse nome.");
        }

        CentroDistribuicao newDistribuitionCenter = new CentroDistribuicao();

        newDistribuitionCenter.setEnterprise(enterprise);
        newDistribuitionCenter.setName(criarCentroDistribuicaoDTO.getName());
        newDistribuitionCenter.setCep(criarCentroDistribuicaoDTO.getCep());
        newDistribuitionCenter.setStreet(criarCentroDistribuicaoDTO.getStreet());
        newDistribuitionCenter.setHouseNumber(criarCentroDistribuicaoDTO.getHouseNumber());
        newDistribuitionCenter.setComplement(criarCentroDistribuicaoDTO.getComplement());
        newDistribuitionCenter.setNeighborhood(criarCentroDistribuicaoDTO.getNeighborhood());
        newDistribuitionCenter.setCity(criarCentroDistribuicaoDTO.getCity());
        newDistribuitionCenter.setState(criarCentroDistribuicaoDTO.getState());

        centroDistribuicaoRepository.save(newDistribuitionCenter);

        return newDistribuitionCenter;
    }

    public List<CentroDistribuicao> getAllDistribuitionCenter (String cnpj) {
        Empresa enterprise = empresaRepository.findByCnpj(cnpj);

        if(enterprise == null) {
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.");
        }

        return centroDistribuicaoRepository.findByEnterpriseCnpj(cnpj);
    }
}
