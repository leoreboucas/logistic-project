# 📦 Logistic API

API REST de rastreamento e gestão de entregas, desenvolvida em Java com Spring Boot. O projeto simula o fluxo completo de uma transportadora: do momento em que o fornecedor cria um pedido até a entrega ao destinatário final, passando por centros de distribuição e múltiplos entregadores.

> Projeto de portfólio desenvolvido om foco em arquitetura de software e boas práticas do ecossistema Java/Spring.

---

## 🚀 Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4 |
| Banco de dados | PostgreSQL |
| ORM | JPA / Hibernate |
| Autenticação | JWT + Spring Security |
| Documentação | Springdoc OpenAPI / Swagger UI |
| Testes | JUnit 5 + Mockito |
| Build | Maven |
| Containerização | Docker + Docker Compose |

---

## 🏗️ Arquitetura

O projeto segue uma **arquitetura em pacotes por domínio** (package-by-feature), onde cada contexto de negócio agrupa suas próprias entidades, repositórios, serviços, controllers e DTOs. Essa abordagem favorece coesão e facilita a navegação no código.

```
com.github.leoreboucas/
├── auth/
├── centrodistribuicao/
├── cliente/
├── empresa/
├── entregador/
├── entregafinal/
├── entregaparcial/
├── fornecedor/
├── historicopedido/
├── pedido/
│   └── services/       ← services separados por ator
├── rastreamento/
└── infra/
    ├── exception/
    └── security/
```

A camada `pedido/services` é dividida por ator de negócio (`FornecedorPedidoService`, `EmpresaPedidoService`, `EntregadorPedidoService`), evitando um service monolítico e tornando as responsabilidades explícitas.

---

## 🔄 Fluxo de status de um pedido

```
Fornecedor cria pedido
    ↓ AGUARDANDO_POSTAGEM

Empresa confirma postagem
    ↓ POSTADO

Empresa confirma triagem
    ↓ EM_TRIAGEM

Empresa envia para transporte  →  cria EntregaParcial
    ↓ EM_TRANSITO

Entregador confirma chegada no centro transacional
    ↓ EM_TRANSITO  (registrado no histórico)

Empresa libera para próximo trecho  →  nova EntregaParcial
    ↓ EM_TRANSITO

Entregador confirma chegada no centro de última milha
    ↓ EM_DISTRIBUICAO

Empresa envia para entrega final  →  cria EntregaFinal
    ↓ SAIU_PARA_ENTREGA  (ou DEVOLVIDO se tentativas excedidas)

Entregador registra tentativa
    → SUCESSO  →  ENTREGUE
    → FRACASSO  →  EM_DISTRIBUICAO  (retorna ao centro)
```

Cada mudança de status é registrada na entidade `ORDER_HISTORY`, que serve como trilha de auditoria completa do pedido.

---

## 🔐 Autenticação

O sistema possui **quatro tipos de atores**, cada um com seu próprio fluxo de login e escopo de permissões:

| Ator | Role | Ações principais |
|---|---|---|
| Fornecedor | `supplier` | Criar e cancelar pedidos |
| Empresa | `enterprise` | Gerenciar centros, entregadores e transições de status |
| Entregador | `delivery_man` | Confirmar chegadas e registrar tentativas de entrega |
| Cliente | `costumer` | Rastrear pedidos |

A autenticação é feita via **JWT stateless**. O token carrega o CPF ou CNPJ do usuário e sua role, que são usados nos controllers para validar identidade e permissão sem consulta adicional ao banco.

---

## 📐 Decisões técnicas relevantes

**Destinatário sem cadastro obrigatório**
O pedido armazena nome e endereço do destinatário diretamente, sem exigir que ele esteja cadastrado no sistema. Isso reflete como transportadoras reais operam.

**EntregaParcial criada apenas na saída**
A entidade representa o trecho de transporte entre centros. É criada quando o pedido *sai* de um centro, não quando chega — simplificando o modelo sem perder rastreabilidade.

**EntregaFinal por tentativa**
A cada nova saída para entrega ao destinatário, um novo registro de `EntregaFinal` é criado. O contador de tentativas fica no `Pedido` e, ao exceder o limite configurável, o sistema muda o status para `DEVOLVIDO` automaticamente.

**Services separados por ator**
Em vez de um `PedidoService` único com centenas de linhas, as regras de negócio ficam em services específicos por quem executa a ação. Isso torna o código mais legível e os testes mais focados.

---

## 🧪 Testes

Testes unitários implementados com **JUnit 5 + Mockito**, organizados com `@Nested` seguindo o padrão AAA (Arrange, Act, Assert).

Testes de integração implementados com **JUnit 5 + TestContainers**, utilizando uma base de teste compartilhada para validar o comportamento end-to-end dos endpoints e fluxos completos.

Cobertura dos principais fluxos de negócio:

- `FornecedorPedidoService` — criação e cancelamento de pedidos
- `EmpresaPedidoService` — confirmações de postagem, triagem, envio e entrega final
- `EntregadorPedidoService` — confirmação de chegada e registro de tentativas
- `PedidoService` — rastreamento e validação de transições de status

---

## 🐳 Como rodar com Docker

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

### Executando

```bash
docker compose up --build
```

A API estará disponível em `http://localhost:8080`.  
A documentação Swagger em `http://localhost:8080/swagger-ui/index.html`.

### Parando

```bash
docker compose down
```

Para remover também os dados do banco:

```bash
docker compose down -v
```

---

## ⚙️ Como rodar localmente (sem Docker)

### Pré-requisitos

- Java 17+
- Maven
- PostgreSQL rodando localmente

### Configuração

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

### Executando

```bash
mvn spring-boot:run
```

---

## 📋 Principais endpoints

A documentação completa e interativa está disponível no Swagger UI após subir a aplicação.

### Públicos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/fornecedores` | Cadastrar fornecedor |
| `POST` | `/clientes` | Cadastrar cliente |
| `POST` | `/empresas` | Cadastrar empresa |
| `POST` | `/auth/login/fornecedor` | Login do fornecedor |
| `POST` | `/auth/login/cliente` | Login do cliente |
| `POST` | `/auth/login/empresa` | Login da empresa |
| `POST` | `/auth/login/entregador` | Login do entregador |
| `GET` | `/rastreamento/{trackingCode}` | Rastrear pedido |

### Protegidos (requerem JWT)

| Método | Rota | Role |
|---|---|---|
| `POST` | `/pedidos` | supplier |
| `PATCH` | `/pedidos/{id}/cancelar` | supplier |
| `PATCH` | `/pedidos/{id}/confirmar-postagem` | enterprise |
| `PATCH` | `/pedidos/{id}/confirmar-triagem` | enterprise |
| `PATCH` | `/pedidos/{id}/confirmar-envio` | enterprise |
| `PATCH` | `/pedidos/{id}/saiu-para-entrega` | enterprise |
| `POST` | `/entregadores` | enterprise |
| `POST` | `/centro-distribuicoes` | enterprise |
| `PATCH` | `/pedidos/{id}/confirmar-chegada` | delivery_man |
| `PATCH` | `/pedidos/{id}/tentativa-entrega` | delivery_man |
| `GET` | `/entregas-parciais` | delivery_man |

---

## 🗺️ Roadmap

- [x] Documentação Swagger/OpenAPI
- [x] Containerização com Docker
- [ ] Soft delete com `@SQLRestriction`

---

## 👤 Autor

**Leonardo Rebouças**  
[github.com/leoreboucas](https://github.com/leoreboucas)
