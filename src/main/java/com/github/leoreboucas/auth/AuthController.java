package com.github.leoreboucas.auth;

import com.github.leoreboucas.empresa.DTO.LoginEmpresaDTO;
import com.github.leoreboucas.empresa.EmpresaService;
import com.github.leoreboucas.fornecedor.DTO.LoginFornecedorDTO;
import com.github.leoreboucas.fornecedor.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
public class AuthController {
    private final FornecedorService fornecedorService;
    private final EmpresaService empresaService;
    public AuthController (FornecedorService fornecedorService, EmpresaService empresaService) {
        this.fornecedorService = fornecedorService;
        this.empresaService = empresaService;
    }

    @PostMapping("/fornecedor")
    @ResponseBody
    public String loginFornecedor(@RequestBody @Valid LoginFornecedorDTO loginFornecedorDTO) {
        return fornecedorService.login(loginFornecedorDTO);
    }

    @PostMapping("/empresa")
    @ResponseBody
    public String loginEmpresa(@RequestBody @Valid LoginEmpresaDTO loginEmpresaDTO) {
        return empresaService.login(loginEmpresaDTO);
    }
}
