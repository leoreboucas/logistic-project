package com.github.leoreboucas.empresa;
import com.github.leoreboucas.empresa.DTO.CriarEmpresaDTO;
import com.github.leoreboucas.empresa.DTO.LoginEmpresaDTO;
import com.github.leoreboucas.infra.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public EmpresaService(EmpresaRepository empresaRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(LoginEmpresaDTO loginEmpresaDTO) {
        Empresa enterprise = empresaRepository.findByCnpj(loginEmpresaDTO.getCnpj());

        if(enterprise == null || !passwordEncoder.matches(loginEmpresaDTO.getPassword(), enterprise.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cnpj e/ou Senha inválidos! Tente novamente.");
        }

        return jwtService.generateToken(enterprise.getCnpj(), "enterprise");
    };

    public Empresa register(CriarEmpresaDTO criarEmpresaDTO) {
        Optional<Empresa> existingEnterprise = Optional.ofNullable(empresaRepository.findByCnpj(criarEmpresaDTO.getCnpj()));

        if (existingEnterprise.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe usuário registrado com esse CNPJ.");
        }

        Empresa newEnterprise = new Empresa();
        newEnterprise.setName(criarEmpresaDTO.getName());
        newEnterprise.setCnpj(criarEmpresaDTO.getCnpj());
        newEnterprise.setPassword(passwordEncoder.encode(criarEmpresaDTO.getPassword()));
        newEnterprise.setCellNumber(criarEmpresaDTO.getCellNumber());
        newEnterprise.setCep(criarEmpresaDTO.getCep());
        newEnterprise.setStreet(criarEmpresaDTO.getStreet());
        newEnterprise.setHouseNumber(criarEmpresaDTO.getHouseNumber());
        newEnterprise.setComplement(criarEmpresaDTO.getComplement());
        newEnterprise.setNeighborhood(criarEmpresaDTO.getNeighborhood());
        newEnterprise.setCity(criarEmpresaDTO.getCity());
        newEnterprise.setState(criarEmpresaDTO.getState());

        empresaRepository.save(newEnterprise);

        return newEnterprise;
    }
}
