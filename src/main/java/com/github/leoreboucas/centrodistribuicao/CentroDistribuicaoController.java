package com.github.leoreboucas.centrodistribuicao;

import com.github.leoreboucas.centrodistribuicao.DTO.CentroDistribuicaoResponse;
import com.github.leoreboucas.centrodistribuicao.DTO.CriarCentroDistribuicaoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/centro-distribuicoes")
public class CentroDistribuicaoController {
    private final CentroDistribuicaoService centroDistribuicaoService;

    public CentroDistribuicaoController(CentroDistribuicaoService centroDistribuicaoService) {
        this.centroDistribuicaoService = centroDistribuicaoService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public CentroDistribuicaoResponse registerController (@RequestBody @Valid CriarCentroDistribuicaoDTO criarCentroDistribuicaoDTO) {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        CentroDistribuicao distribuitionCenter = centroDistribuicaoService.register(criarCentroDistribuicaoDTO, cnpj);

        return new CentroDistribuicaoResponse(
                distribuitionCenter.getName(),
                distribuitionCenter.getCep(),
                distribuitionCenter.getStreet(),
                distribuitionCenter.getHouseNumber(),
                distribuitionCenter.getComplement(),
                distribuitionCenter.getNeighborhood(),
                distribuitionCenter.getCity(),
                distribuitionCenter.getState());
    }

    @GetMapping
    public List<CentroDistribuicaoResponse> getAllDistribuitionCenterController () {
        String cnpj = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        List<CentroDistribuicao> distribuitionCenters = centroDistribuicaoService.getAllDistribuitionCenter(cnpj);

        return distribuitionCenters.stream()
                .map(center -> new CentroDistribuicaoResponse(
                        center.getName(),
                        center.getCep(),
                        center.getStreet(),
                        center.getHouseNumber(),
                        center.getComplement(),
                        center.getNeighborhood(),
                        center.getCity(),
                        center.getState()
                ))
                .collect(Collectors.toList());

    }
}
