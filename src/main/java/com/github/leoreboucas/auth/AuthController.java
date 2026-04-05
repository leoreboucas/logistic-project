package com.github.leoreboucas.auth;

import com.github.leoreboucas.cliente.ClienteService;
import com.github.leoreboucas.cliente.DTO.LoginClienteDTO;
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
    private final ClienteService clienteService;
    public AuthController (FornecedorService fornecedorService, EmpresaService empresaService, ClienteService clienteService) {
        this.fornecedorService = fornecedorService;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
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

    @PostMapping("/cliente")
    @ResponseBody
    public String loginCliente(@RequestBody @Valid LoginClienteDTO loginClienteDTO) {
        return clienteService.login(loginClienteDTO);
    }
}
