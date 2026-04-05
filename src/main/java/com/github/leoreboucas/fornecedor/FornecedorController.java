package com.github.leoreboucas.fornecedor;

import com.github.leoreboucas.fornecedor.DTO.CriarFornecedorDTO;
import com.github.leoreboucas.fornecedor.DTO.FornecedorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {
    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponseDTO registerFornecedor(@RequestBody @Valid CriarFornecedorDTO criarFornecedorDTO) {
        Fornecedor fornecedor = fornecedorService.register(criarFornecedorDTO);
        return new FornecedorResponseDTO(fornecedor.getName(), fornecedor.getCnpj());
    }
}
