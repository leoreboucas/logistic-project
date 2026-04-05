package com.github.leoreboucas.auth;

import com.github.leoreboucas.fornecedor.DTO.LoginFornecedorDTO;
import com.github.leoreboucas.fornecedor.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
public class AuthController {
    private final FornecedorService fornecedorService;
    public AuthController (FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping("/fornecedor")
    @ResponseBody
    public String loginFornecedor(@RequestBody @Valid LoginFornecedorDTO loginFornecedorDTO) {
        return fornecedorService.login(loginFornecedorDTO);
    }

}
