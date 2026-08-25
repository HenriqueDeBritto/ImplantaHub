# ImplantaHub

Backend para gerenciamento de processos de implantação de software e suporte a clientes, desenvolvido em Java com Spring Boot.

O projeto também possui finalidade de estudo e portfólio, buscando aplicar tecnologias, padrões e práticas utilizadas em aplicações Java corporativas.

## Objetivo

O ImplantaHub pretende centralizar o acompanhamento do ciclo de implantação de sistemas em clientes.

Entre as funcionalidades planejadas estão:

* gerenciamento de clientes;
* gerenciamento de usuários;
* projetos de implantação;
* etapas e checklists de implantação;
* responsáveis pelas implantações;
* chamados de suporte;
* comentários em chamados;
* controle de status;
* controle de SLA;
* histórico e auditoria.

O desenvolvimento está sendo realizado de forma incremental, evitando a implementação de toda a aplicação de uma única vez.

## Tecnologias

### Atualmente utilizadas

* Java 21
* Spring Boot 4.1.1
* Maven
* Spring Web MVC
* Bean Validation
* Spring Data JPA
* PostgreSQL JDBC Driver
* Flyway
* Git

### Planejadas

* PostgreSQL
* Docker
* Docker Compose
* Spring Security
* JUnit 5
* Mockito
* Testcontainers
* OpenAPI / Swagger
* GitHub Actions

## Arquitetura

O projeto começa como um **monólito modular**, organizado principalmente por domínio/feature.

Estrutura prevista:

```text
com.henrique.implantahub
├── client
├── implementation
├── ticket
├── user
├── audit
├── config
└── exception
```

A aplicação não utiliza microsserviços neste estágio.

## Estrutura atual

Atualmente o projeto possui apenas a fundação da aplicação.

```text
src/main/java/com/henrique/implantahub
├── ImplantaHubApplication.java
└── health
    └── HealthController.java
```

O primeiro endpoint disponível é um health check utilizado para verificar se a aplicação está funcionando.

### Health Check

```http
GET /api/health
```

Resposta:

```text
ImplantaHub API is running
```

## Banco de dados

O projeto utilizará PostgreSQL.

O ambiente local será executado através de Docker Compose.

Configuração planejada para desenvolvimento:

```text
Database: implantahub
User: implantahub
Port: 5432
```

As alterações estruturais do banco serão versionadas através do **Flyway**.

O Hibernate não será utilizado como mecanismo principal de criação automática das tabelas.

## Docker Compose

O arquivo `compose.yaml` presente na raiz do projeto será responsável por executar o PostgreSQL local.

Quando o Docker estiver instalado:

```bash
docker compose up -d
```

Para verificar os containers:

```bash
docker compose ps
```

Para visualizar os logs do PostgreSQL:

```bash
docker compose logs -f postgres
```

Para parar o ambiente sem apagar os dados:

```bash
docker compose stop
```

Para remover os containers e também o volume de dados:

```bash
docker compose down -v
```

> O comando `down -v` remove o volume do PostgreSQL e, consequentemente, os dados armazenados nele. Utilize-o apenas quando realmente desejar recriar o banco local.

## Executando a aplicação

No Windows, utilizando o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Também é possível executar diretamente pela classe:

```text
ImplantaHubApplication
```

através do IntelliJ IDEA.

Por padrão, a aplicação utiliza:

```text
http://localhost:8080
```

## Build

Executar os testes:

```powershell
.\mvnw.cmd clean test
```

Gerar o pacote da aplicação:

```powershell
.\mvnw.cmd clean package
```

O artefato gerado ficará no diretório:

```text
target/
```

## Status do desenvolvimento

### Fundação

* [x] Projeto Spring Boot
* [x] Java 21
* [x] Maven
* [x] Spring Web MVC
* [x] Bean Validation
* [x] Health check
* [x] Git
* [x] Dependências de persistência
* [x] Estrutura inicial do Docker Compose
* [ ] PostgreSQL local em execução
* [ ] Configuração do datasource
* [ ] Primeira migration Flyway

### Domínio Client

* [ ] Entidade Client
* [ ] ClientStatus
* [ ] ClientRepository
* [ ] DTOs
* [ ] ClientService
* [ ] tratamento de exceções
* [ ] ClientController
* [ ] testes

### Futuro

* [ ] Implantações
* [ ] Etapas e checklists
* [ ] Chamados
* [ ] SLA
* [ ] Usuários
* [ ] Spring Security
* [ ] Auditoria
* [ ] Testcontainers
* [ ] OpenAPI / Swagger
* [ ] CI/CD

## Princípios do projeto

Algumas decisões adotadas durante o desenvolvimento:

* organização por domínio/feature;
* controllers sem regras de negócio;
* entidades JPA não expostas diretamente pela API;
* utilização de DTOs para comunicação HTTP;
* schema do banco versionado através do Flyway;
* regras de negócio concentradas na camada de serviço;
* testes para regras relevantes;
* evitar dependências desnecessárias;
* evitar overengineering;
* implementação incremental;
* monólito modular antes de considerar microsserviços.

## Autor

Desenvolvido como projeto de estudo e portfólio em Java e Spring Boot.
