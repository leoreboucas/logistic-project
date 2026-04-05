package com.github.leoreboucas.cliente;

import com.github.leoreboucas.cliente.DTO.ClienteResponseDTO;
import com.github.leoreboucas.cliente.DTO.CriarClienteDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)

    public ClienteResponseDTO registerCliente (@RequestBody @Valid CriarClienteDTO criarClienteDTO) {
        Cliente cliente = clienteService.register(criarClienteDTO);
        return new ClienteResponseDTO(cliente.getFirstName(), cliente.getSecondName(), cliente.getCpf());
    }
}
