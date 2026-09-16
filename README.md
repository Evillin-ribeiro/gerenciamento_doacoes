# Sistema de Gerenciamento de Doações

Sistema web para gestão de doações (itens físicos e financeiras), agendamento de entregas
presenciais, controle de estoque e distribuição a beneficiários — desenvolvido para a
**Associação Espírita de Estudos Evangélicos Francisco de Paula Vitor**.

O sistema tem dois fluxos principais:

- **Fluxo público do doador**: cadastro, registro da doação (item físico ou Pix), envio de
  comprovante e agendamento de entrega presencial — sem necessidade de login.
- **Painel administrativo**: gestão de doadores, doações, estoque, distribuições, horários de
  atendimento, usuários e relatórios — acesso restrito (ADMIN/VOLUNTÁRIO), autenticado via JWT.

## Stack

Java 17 · Spring Boot 4 (Web, Data JPA, Security) · MySQL 8 · Flyway · Thymeleaf + JS vanilla ·
JWT · JUnit 5/Mockito/MockMvc (testes unitários e de integração, estes últimos com H2)

## Como rodar (Docker)

Pré-requisito: Docker e Docker Compose.

```bash
cp .env.example .env
# edite o .env e defina MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD e JWT_SECRET

docker compose up -d --build
```

A aplicação sobe em `http://localhost:8080` (ou `http://<ip-da-maquina>:8080` a partir de outro
dispositivo na mesma rede). O banco e o schema são criados automaticamente pelo Flyway na
primeira subida, incluindo um usuário administrador inicial — veja
`src/main/resources/db/migration/V4__seed_admin_usuario.sql` para as credenciais seedadas
(troque a senha após o primeiro login).

## Como rodar sem Docker

Pré-requisitos: JDK 17+ e um MySQL 8 acessível.

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# edite com a senha do seu MySQL e gere um segredo JWT (openssl rand -base64 48)

./mvnw spring-boot:run
```

## Testes

```bash
./mvnw test
```

Cobre regras de negócio (testes unitários com Mockito) e os fluxos ponta a ponta — autenticação,
doação de item físico, doação financeira, agendamento e distribuição — via testes de integração
com MockMvc contra um banco H2 em memória (não precisa de MySQL para rodar a suíte).
