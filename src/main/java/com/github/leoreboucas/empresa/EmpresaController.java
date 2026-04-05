package com.github.leoreboucas.empresa;

import com.github.leoreboucas.empresa.DTO.CriarEmpresaDTO;
import com.github.leoreboucas.empresa.DTO.EmpresaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaResponseDTO registerEmpresa(@RequestBody @Valid CriarEmpresaDTO criarEmpresaDTO) {
        Empresa enterprise = empresaService.register(criarEmpresaDTO);

        return new EmpresaResponseDTO(enterprise.getName(), enterprise.getCnpj(), enterprise.getCreatedAt());
    }
}
