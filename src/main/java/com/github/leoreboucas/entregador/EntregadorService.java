package com.github.leoreboucas.entregador;

import com.github.leoreboucas.empresa.Empresa;
import com.github.leoreboucas.empresa.EmpresaRepository;
import com.github.leoreboucas.entregador.DTO.CriarEntregadorDTO;
import com.github.leoreboucas.entregador.DTO.LoginEntregadorDTO;
import com.github.leoreboucas.infra.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class EntregadorService {
    private final EntregadorRepository entregadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmpresaRepository empresaRepository;

    public EntregadorService(EntregadorRepository entregadorRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmpresaRepository empresaRepository) {
        this.entregadorRepository = entregadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.empresaRepository = empresaRepository;
    }

    public String login(LoginEntregadorDTO loginEntregadorDTO) {
        Entregador deliveryMan = entregadorRepository.findByCpf(loginEntregadorDTO.getCpf());

        if (deliveryMan == null || !passwordEncoder.matches(loginEntregadorDTO.getPassword(), deliveryMan.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CPF e/ou Senha inválidos! Tente novamente.");
        }

        return jwtService.generateToken(deliveryMan.getCpf(), "delivery_man");
    }

    public Entregador register(CriarEntregadorDTO criarEntregadorDTO, String cnpjEnterprise){
        Optional<Entregador> existingDeliveryMan = Optional.ofNullable(entregadorRepository.findByCpf(criarEntregadorDTO.getCpf()));
        Empresa enterprise = empresaRepository.findByCnpj(cnpjEnterprise);

        if (enterprise == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.");
        }

        if (existingDeliveryMan.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe usuário registrado com esse CPF.");
        }

        Entregador newDeliveryMan = new Entregador();
        newDeliveryMan.setEnterprise(enterprise);
        newDeliveryMan.setFirstName(criarEntregadorDTO.getFirstName());
        newDeliveryMan.setSecondName(criarEntregadorDTO.getSecondName());
        newDeliveryMan.setCpf(criarEntregadorDTO.getCpf());
        newDeliveryMan.setPassword(passwordEncoder.encode(criarEntregadorDTO.getPassword()));
        newDeliveryMan.setDateOfBirth(criarEntregadorDTO.getDateOfBirth());
        newDeliveryMan.setCellNumber(criarEntregadorDTO.getCellNumber());
        newDeliveryMan.setCep(criarEntregadorDTO.getCep());
        newDeliveryMan.setStreet(criarEntregadorDTO.getStreet());
        newDeliveryMan.setComplement(criarEntregadorDTO.getComplement());
        newDeliveryMan.setHouseNumber(criarEntregadorDTO.getHouseNumber());
        newDeliveryMan.setNeighborhood(criarEntregadorDTO.getNeighborhood());
        newDeliveryMan.setCity(criarEntregadorDTO.getCity());
        newDeliveryMan.setState(criarEntregadorDTO.getState());
        newDeliveryMan.setCnhCategory(criarEntregadorDTO.getCnhCategory());
        newDeliveryMan.setVehicleType(criarEntregadorDTO.getVehicleType());
        newDeliveryMan.setAvailability(criarEntregadorDTO.getAvailability());
        newDeliveryMan.setCapacity(criarEntregadorDTO.getCapacity());
        newDeliveryMan.setDeliveryManType(criarEntregadorDTO.getDeliveryManType());

        entregadorRepository.save(newDeliveryMan);

        return newDeliveryMan;
    }
}
