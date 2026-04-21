package com.github.leoreboucas.pedido.controllers;

import com.github.leoreboucas.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import(JacksonAutoConfiguration.class)
@AutoConfigureJson
public class ControllerTest extends IntegrationTestBase {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldCompleteOrderFlow () throws Exception {
        String registerSupplierBody = """
                {
                  "name": "Transportadora Norte",
                  "cnpj": "98765432000110",
                  "email": "fornecedor@email.com",
                  "password": "senha123",
                  "cellNumber": "71988887777",
                  "cep": "40020000",
                  "street": "Rua Chile",
                  "houseNumber": "500",
                  "complement": "",
                  "neighborhood": "Comércio",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/fornecedores").contentType(MediaType.APPLICATION_JSON).content(registerSupplierBody))
                .andExpect(status().isCreated());

        String loginBody = """
                    {
                      "cnpj": "98765432000110",
                      "password": "senha123"
                    }
                """;

        String supplierToken = mockMvc.perform(
                        post("/auth/login/fornecedor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createOrderBody = """
                {
                  "customerCompleteName": "João da Silva Sauro",
                  "cellNumber": "11988887777",
                  "cep": "01001000",
                  "street": "Praça da Sé",
                  "houseNumber": "123",
                  "complement": "Apto 42",
                  "neighborhood": "Sé",
                  "city": "São Paulo",
                  "state": "SP",
                  "weight": 1.500,
                  "length": 20.0,
                  "width": 15.0,
                  "depth": 10.0,
                  "observation": "Entregar na recepção do prédio."
                }
                
                """;

        String orderResponse = mockMvc.perform(post("/pedidos")
                .header("Authorization", "Bearer "+ supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(orderResponse);
        String trackingCode = node.get("trackingCode").asString();

        String registerEnterpriseBody = """
                {
                  "name": "Transportadora Express",
                  "cnpj": "11222333000181",
                  "email": "express@express.com",
                  "password": "senha123",
                  "cellNumber": "71977778888",
                  "cep": "41820000",
                  "street": "Avenida Paralela",
                  "houseNumber": "200",
                  "complement": "",
                  "neighborhood": "Trobogy",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content(registerEnterpriseBody))
                .andExpect(status().isCreated());

        String enterpriseLoginBody = """
                    {
                      "cnpj": "11222333000181",
                      "password": "senha123"
                    }
                """;

        String enterpriseResponse = mockMvc.perform(
                        post("/auth/login/empresa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(enterpriseLoginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createDeliveryManBody = """
                    {
                      "firstName": "Carlos",
                      "secondName": "Mendes",
                      "cpf": "98765432100",
                      "email": "carlos.entregador@express.com",
                      "password": "entrega123",
                      "dateOfBirth": "1988-03-20T00:00:00",
                      "cellNumber": "71966665555",
                      "cep": "41820000",
                      "street": "Avenida Paralela",
                      "complement": "",
                      "houseNumber": "350",
                      "neighborhood": "Trobogy",
                      "city": "Salvador",
                      "state": "BA",
                      "cnhCategory": "B",
                      "vehicleType": "VEICULO_LEVE",
                      "availability": "DISPONIVEL",
                      "capacity": 500.0,
                      "deliveryManType": "TRANSFERENCIA"
                    }
                """;

        mockMvc.perform(post("/entregadores")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDeliveryManBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Origem SP", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Transbordo RJ", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Salvador Final", "ULTIMA_MILHA"))
        ).andExpect(status().isCreated());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-postagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("POSTADO"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-triagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_TRIAGEM"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-envio", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Origem SP",
                        "destinationCenter": "Centro Transbordo RJ"
                    }
                    """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_TRANSITO"));

        String deliveryManLoginBody = """
                    {
                      "cpf": "98765432100",
                      "password": "entrega123"
                    }
                """;

        String deliveryManResponse = mockMvc.perform(
                        post("/auth/login/entregador")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(deliveryManLoginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-chegada", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_TRANSITO"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-envio", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Transbordo RJ",
                        "destinationCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_TRANSITO"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-chegada", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_DISTRIBUICAO"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SAIU_PARA_ENTREGA"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "FRACASSO"
                        }
                        """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_DISTRIBUICAO"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SAIU_PARA_ENTREGA"));

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "SUCESSO"
                        }
                        """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void shouldCancelOrderFlow () throws Exception {
        String registerSupplierBody = """
                {
                  "name": "Transportadora Norte",
                  "cnpj": "98765432000110",
                  "email": "fornecedor@email.com",
                  "password": "senha123",
                  "cellNumber": "71988887777",
                  "cep": "40020000",
                  "street": "Rua Chile",
                  "houseNumber": "500",
                  "complement": "",
                  "neighborhood": "Comércio",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/fornecedores").contentType(MediaType.APPLICATION_JSON).content(registerSupplierBody))
                .andExpect(status().isCreated());

        String loginBody = """
                    {
                      "cnpj": "98765432000110",
                      "password": "senha123"
                    }
                """;

        String supplierToken = mockMvc.perform(
                        post("/auth/login/fornecedor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createOrderBody = """
                {
                  "customerCompleteName": "João da Silva Sauro",
                  "cellNumber": "11988887777",
                  "cep": "01001000",
                  "street": "Praça da Sé",
                  "houseNumber": "123",
                  "complement": "Apto 42",
                  "neighborhood": "Sé",
                  "city": "São Paulo",
                  "state": "SP",
                  "weight": 1.500,
                  "length": 20.0,
                  "width": 15.0,
                  "depth": 10.0,
                  "observation": "Entregar na recepção do prédio."
                }
                
                """;

        String orderResponse = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer "+ supplierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(orderResponse);
        String trackingCode = node.get("trackingCode").asString();

        String registerEnterpriseBody = """
                {
                  "name": "Transportadora Express",
                  "cnpj": "11222333000181",
                  "email": "express@express.com",
                  "password": "senha123",
                  "cellNumber": "71977778888",
                  "cep": "41820000",
                  "street": "Avenida Paralela",
                  "houseNumber": "200",
                  "complement": "",
                  "neighborhood": "Trobogy",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content(registerEnterpriseBody))
                .andExpect(status().isCreated());

        String enterpriseLoginBody = """
                    {
                      "cnpj": "11222333000181",
                      "password": "senha123"
                    }
                """;

        String enterpriseResponse = mockMvc.perform(
                        post("/auth/login/empresa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(enterpriseLoginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createDeliveryManBody = """
                    {
                      "firstName": "Carlos",
                      "secondName": "Mendes",
                      "cpf": "98765432100",
                      "email": "carlos.entregador@express.com",
                      "password": "entrega123",
                      "dateOfBirth": "1988-03-20T00:00:00",
                      "cellNumber": "71966665555",
                      "cep": "41820000",
                      "street": "Avenida Paralela",
                      "complement": "",
                      "houseNumber": "350",
                      "neighborhood": "Trobogy",
                      "city": "Salvador",
                      "state": "BA",
                      "cnhCategory": "B",
                      "vehicleType": "VEICULO_LEVE",
                      "availability": "DISPONIVEL",
                      "capacity": 500.0,
                      "deliveryManType": "TRANSFERENCIA"
                    }
                """;

        mockMvc.perform(post("/entregadores")
                        .header("Authorization", "Bearer " + enterpriseResponse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createDeliveryManBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Origem SP", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Transbordo RJ", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Salvador Final", "ULTIMA_MILHA"))
        ).andExpect(status().isCreated());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-postagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-triagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-envio", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Origem SP",
                        "destinationCenter": "Centro Transbordo RJ"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/cancelar", trackingCode)
                .header("Authorization", "Bearer " + supplierToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void shouldReturnOrderFlow () throws Exception {
        String registerSupplierBody = """
                {
                  "name": "Transportadora Norte",
                  "cnpj": "98765432000110",
                  "email": "fornecedor@email.com",
                  "password": "senha123",
                  "cellNumber": "71988887777",
                  "cep": "40020000",
                  "street": "Rua Chile",
                  "houseNumber": "500",
                  "complement": "",
                  "neighborhood": "Comércio",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/fornecedores").contentType(MediaType.APPLICATION_JSON).content(registerSupplierBody))
                .andExpect(status().isCreated());

        String loginBody = """
                    {
                      "cnpj": "98765432000110",
                      "password": "senha123"
                    }
                """;

        String supplierToken = mockMvc.perform(
                        post("/auth/login/fornecedor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createOrderBody = """
                {
                  "customerCompleteName": "João da Silva Sauro",
                  "cellNumber": "11988887777",
                  "cep": "01001000",
                  "street": "Praça da Sé",
                  "houseNumber": "123",
                  "complement": "Apto 42",
                  "neighborhood": "Sé",
                  "city": "São Paulo",
                  "state": "SP",
                  "weight": 1.500,
                  "length": 20.0,
                  "width": 15.0,
                  "depth": 10.0,
                  "observation": "Entregar na recepção do prédio."
                }
                
                """;

        String orderResponse = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer "+ supplierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(orderResponse);
        String trackingCode = node.get("trackingCode").asString();

        String registerEnterpriseBody = """
                {
                  "name": "Transportadora Express",
                  "cnpj": "11222333000181",
                  "email": "express@express.com",
                  "password": "senha123",
                  "cellNumber": "71977778888",
                  "cep": "41820000",
                  "street": "Avenida Paralela",
                  "houseNumber": "200",
                  "complement": "",
                  "neighborhood": "Trobogy",
                  "city": "Salvador",
                  "state": "BA"
                }
                """;

        mockMvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content(registerEnterpriseBody))
                .andExpect(status().isCreated());

        String enterpriseLoginBody = """
                    {
                      "cnpj": "11222333000181",
                      "password": "senha123"
                    }
                """;

        String enterpriseResponse = mockMvc.perform(
                        post("/auth/login/empresa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(enterpriseLoginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createDeliveryManBody = """
                    {
                      "firstName": "Carlos",
                      "secondName": "Mendes",
                      "cpf": "98765432100",
                      "email": "carlos.entregador@express.com",
                      "password": "entrega123",
                      "dateOfBirth": "1988-03-20T00:00:00",
                      "cellNumber": "71966665555",
                      "cep": "41820000",
                      "street": "Avenida Paralela",
                      "complement": "",
                      "houseNumber": "350",
                      "neighborhood": "Trobogy",
                      "city": "Salvador",
                      "state": "BA",
                      "cnhCategory": "B",
                      "vehicleType": "VEICULO_LEVE",
                      "availability": "DISPONIVEL",
                      "capacity": 500.0,
                      "deliveryManType": "TRANSFERENCIA"
                    }
                """;

        mockMvc.perform(post("/entregadores")
                        .header("Authorization", "Bearer " + enterpriseResponse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createDeliveryManBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Origem SP", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Transbordo RJ", "TRANSACIONAL"))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/centro-distribuicoes")
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDistribuitionCenterBody("Centro Salvador Final", "ULTIMA_MILHA"))
        ).andExpect(status().isCreated());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-postagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-triagem", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-envio", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Origem SP",
                        "destinationCenter": "Centro Transbordo RJ"
                    }
                    """)
        ).andExpect(status().isOk());

        String deliveryManLoginBody = """
                    {
                      "cpf": "98765432100",
                      "password": "entrega123"
                    }
                """;

        String deliveryManResponse = mockMvc.perform(
                        post("/auth/login/entregador")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(deliveryManLoginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-chegada", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-envio", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Transbordo RJ",
                        "destinationCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/confirmar-chegada", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "FRACASSO"
                        }
                        """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "FRACASSO"
                        }
                        """)
        ).andExpect(status().isOk());
        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "FRACASSO"
                        }
                        """)
        ).andExpect(status().isOk());
        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/tentativa-entrega", trackingCode)
                .header("Authorization", "Bearer " + deliveryManResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "FRACASSO"
                        }
                        """)
        ).andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/{trackingCode}/saiu-para-entrega", trackingCode)
                .header("Authorization", "Bearer " + enterpriseResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "deliveryManCpf": "98765432100",
                        "originCenter": "Centro Salvador Final"
                    }
                    """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DEVOLVIDO"));
    }

    private String createDistribuitionCenterBody(String name, String tipo) {
        return """
            {
                "name": "%s",
                "cep": "01310100",
                "street": "Avenida Paulista",
                "houseNumber": "1000",
                "complement": "",
                "neighborhood": "Bela Vista",
                "city": "São Paulo",
                "state": "SP",
                "centerDistribuitionType": "%s"
            }
            """.formatted(name, tipo);
    }
}
