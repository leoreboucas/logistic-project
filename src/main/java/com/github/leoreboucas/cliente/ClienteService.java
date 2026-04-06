package com.github.leoreboucas.cliente;

import com.github.leoreboucas.cliente.DTO.CriarClienteDTO;
import com.github.leoreboucas.cliente.DTO.LoginClienteDTO;
import com.github.leoreboucas.infra.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(LoginClienteDTO loginClienteDTO) {
        Cliente costumer = clienteRepository.findByCpf(loginClienteDTO.getCpf());

        if (costumer == null || !passwordEncoder.matches(loginClienteDTO.getPassword(), costumer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CPF e/ou Senha inválidos! Tente novamente.");
        }

        return jwtService.generateToken(costumer.getCpf(), "costumer");
    }

    public Cliente register (CriarClienteDTO criarClienteDTO) {
        Optional<Cliente> existingCustomer = Optional.ofNullable(clienteRepository.findByCpf(criarClienteDTO.getCpf()));

        if(existingCustomer.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe usuário registrado com esse CPF.");
        }

        Cliente newCostumer = new Cliente();

        newCostumer.setFirstName(criarClienteDTO.getFirstName());
        newCostumer.setSecondName(criarClienteDTO.getSecondName());
        newCostumer.setCpf(criarClienteDTO.getCpf());
        newCostumer.setPassword(passwordEncoder.encode(criarClienteDTO.getPassword()));
        newCostumer.setDateOfBirth(criarClienteDTO.getDateOfBirth());
        newCostumer.setCellNumber(criarClienteDTO.getCellNumber());
        newCostumer.setCep(criarClienteDTO.getCep());
        newCostumer.setCity(criarClienteDTO.getCity());
        newCostumer.setComplement(criarClienteDTO.getComplement());
        newCostumer.setHouseNumber(criarClienteDTO.getHouseNumber());
        newCostumer.setNeighborhood(criarClienteDTO.getNeighborhood());
        newCostumer.setStreet(criarClienteDTO.getStreet());
        newCostumer.setState(criarClienteDTO.getState());

        clienteRepository.save(newCostumer);

        return newCostumer;
    }
}
