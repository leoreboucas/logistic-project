# 📦 API de Rastreamento e Gestão de Entregas

> 🚧 **Atenção:** Este projeto está **em desenvolvimento ativo**. Estruturas, endpoints e regras de negócio podem ser alterados a qualquer momento. **Ainda não está pronto para uso em produção ou execução local.**

## 📖 Sobre o Projeto
Backend RESTful desenvolvido para gerenciar o ciclo completo de entregas logísticas, desde a criação do pedido até a confirmação de recebimento pelo cliente. O sistema permite rastreamento público, controle de rotas com múltiplos centros de distribuição e gestão granular por perfis de usuário, seguindo boas práticas de arquitetura e segurança.

## 🛠️ Stack Tecnológica
- **Java** + **Spring Boot**
- **PostgreSQL**
- **JPA / Hibernate**
- **JWT** (Autenticação e autorização)
- *(Docker, Swagger/OpenAPI e scripts de deploy serão integrados nas próximas fases)*

## ✨ Funcionalidades em Implementação
- 🔍 Rastreamento público via código único (sem autenticação)
- 🔄 Máquina de estados para acompanhar o ciclo de vida do pedido
- 📦 Entregas parciais para gerenciar trechos de rota
- 👥 Controle de acesso por perfil (`Empresa`, `Fornecedor`, `Entregador`, `Público`)
- 📜 Histórico automático de transições de status
- 🛡️ Validação de regras de negócio (cancelamento por fase, tipos de entrega, `soft deletes`, etc.)

## 📡 Estrutura da API (Planejada)
A API seguirá padrões REST e está sendo organizada por contexto:
- `/auth` → Autenticação
- `/rastreamento` → Consulta pública
- `/pedidos` → Criação, consulta e cancelamento
- `/entregas-parciais` → Atribuição, conclusão e registro de tentativas
- `/entregadores` & `/centros` → Cadastros operacionais

## 📊 Status Atual & Como Acompanhar o Código
Este repositório está sendo utilizado principalmente para **documentar a evolução da base de código** e validar a arquitetura. Ainda não há um ambiente configurado para execução.

Para acompanhar o andamento:
- 📂 **Estrutura de pastas:** Navegue por `src/main/java` para ver como entidades, repositórios, serviços e controladores estão sendo organizados.
- 🔍 **Histórico de commits:** Cada commit reflete uma etapa de implementação (modelagem, validações, rotas, regras de negócio, etc.).
- 🧩 **Próximas etapas:** Configuração do banco local via Docker, integração com Swagger, testes automatizados e scripts de inicialização.

## 📌 Observações
- 🔄 O código está em constante refatoração. Algumas classes ou pacotes podem ser reestruturados sem aviso prévio.
- 🤝 Contribuições externas ainda não estão abertas. Este repositório serve como registro de estudo e portfólio técnico.

## 📄 Licença
Distribuído sob a licença [MIT/Apache 2.0/etc.]. Consulte o arquivo `LICENSE` para mais informações.