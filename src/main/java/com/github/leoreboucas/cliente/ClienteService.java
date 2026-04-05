package com.github.leoreboucas.cliente;

import com.github.leoreboucas.cliente.DTO.CriarClienteDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
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
