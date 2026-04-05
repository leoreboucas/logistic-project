package com.github.leoreboucas.fornecedor;

import com.github.leoreboucas.fornecedor.DTO.CriarFornecedorDTO;
import com.github.leoreboucas.fornecedor.DTO.LoginFornecedorDTO;
import com.github.leoreboucas.infra.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class FornecedorService {
    private final FornecedorRepository fornecedorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public FornecedorService (FornecedorRepository fornecedorRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.fornecedorRepository = fornecedorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Fornecedor findByCnpj(String cnpj) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(cnpj);

        if (supplier== null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado");
        }

        return supplier;
    }

    public String login(LoginFornecedorDTO loginFornecedorDTO) {
        Fornecedor supplier = fornecedorRepository.findByCnpj(loginFornecedorDTO.getCnpj());

        if (supplier == null || !passwordEncoder.matches(loginFornecedorDTO.getPassword(), supplier.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cnpj e/ou Senha inválidos! Tente novamente.");
        }

        return jwtService.generateToken(supplier.getCnpj(), "supplier");
    }

    public Fornecedor register(CriarFornecedorDTO criarFornecedorDTO){
        Optional<Fornecedor> existingSupplier = Optional.ofNullable(fornecedorRepository.findByCnpj(criarFornecedorDTO.getCnpj()));

        if (existingSupplier.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe usuário registrado com esse CNPJ.");
        }

        Fornecedor newSupplier = new Fornecedor();
        newSupplier.setName(criarFornecedorDTO.getName());
        newSupplier.setCnpj(criarFornecedorDTO.getCnpj());
        newSupplier.setPassword(passwordEncoder.encode(criarFornecedorDTO.getPassword()));
        newSupplier.setCellNumber(criarFornecedorDTO.getCellNumber());
        newSupplier.setCep(criarFornecedorDTO.getCep());
        newSupplier.setStreet(criarFornecedorDTO.getStreet());
        newSupplier.setHouseNumber(criarFornecedorDTO.getHouseNumber());
        newSupplier.setComplement(criarFornecedorDTO.getComplement());
        newSupplier.setNeighborhood(criarFornecedorDTO.getNeighborhood());
        newSupplier.setCity(criarFornecedorDTO.getCity());
        newSupplier.setState(criarFornecedorDTO.getState());

        fornecedorRepository.save(newSupplier);

        return newSupplier;
    }
}
