
# ImplantaHub

Backend para gerenciamento de processos de implantação de software e suporte a clientes, desenvolvido em Java com Spring Boot.

O projeto também possui finalidade de estudo e portfólio, buscando aplicar tecnologias, padrões arquiteturais e práticas utilizadas em aplicações Java corporativas.

## Objetivo

O ImplantaHub pretende centralizar o acompanhamento do ciclo de implantação de sistemas em clientes, desde o cadastro inicial até a conclusão da implantação e o suporte posterior.

Entre as funcionalidades planejadas estão:

- Gerenciamento de clientes;
- Gerenciamento de usuários;
- Projetos de implantação;
- Etapas e checklists de implantação;
- Responsáveis pelas implantações;
- Chamados de suporte;
- Comentários em chamados;
- Controle de status;
- Controle de SLA;
- Histórico e auditoria.

O desenvolvimento é realizado de forma incremental, implementando e validando cada funcionalidade antes de avançar para a próxima.

## Tecnologias

### Atualmente utilizadas

- Java 21;
- Spring Boot 4.1.1;
- Maven e Maven Wrapper;
- Spring Web MVC;
- Bean Validation;
- Spring Data JPA e Hibernate;
- PostgreSQL 17;
- PostgreSQL JDBC Driver;
- Flyway;
- Docker e Docker Compose;
- JUnit 5;
- Mockito;
- AssertJ;
- MockMvc;
- Git e GitHub.

### Planejadas

- Spring Security;
- Testcontainers;
- OpenAPI / Swagger;
- GitHub Actions.

## Arquitetura

O projeto segue uma arquitetura de monólito modular, organizada principalmente por domínio ou funcionalidade.

A proposta é manter os componentes relacionados a cada funcionalidade próximos uns dos outros, evitando uma organização excessivamente fragmentada durante os estágios iniciais do desenvolvimento.

O domínio de clientes, por exemplo, reúne entidade, Repository, Service, DTOs, Controller e tratamento de exceções dentro do pacote `client`.

As principais responsabilidades são:

- **Controller:** receber requisições HTTP, validar os dados de entrada e delegar as operações;
- **Service:** implementar e coordenar as regras de negócio;
- **Repository:** realizar as operações de persistência por meio do Spring Data JPA;
- **Entity:** representar os dados persistidos e o estado do domínio;
- **DTOs:** definir os contratos de entrada e saída da API;
- **Exception Handler:** traduzir exceções específicas em respostas HTTP apropriadas.

O projeto não utiliza microsserviços neste estágio.

## Estrutura atual

A estrutura principal da aplicação está organizada da seguinte forma:

```text
src/
├── main/
│   ├── java/com/henrique/implantahub/
│   │   ├── ImplantaHubApplication.java
│   │   │
│   │   ├── client/
│   │   │   ├── Client.java
│   │   │   ├── ClientController.java
│   │   │   ├── ClientExceptionHandler.java
│   │   │   ├── ClientRepository.java
│   │   │   ├── ClientResponse.java
│   │   │   ├── ClientService.java
│   │   │   ├── ClientStatus.java
│   │   │   ├── CreateClientRequest.java
│   │   │   └── DuplicateCnpjException.java
│   │   │
│   │   └── health/
│   │       └── HealthController.java
│   │
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           └── V1__create_clients_table.sql
│
└── test/
    └── java/com/henrique/implantahub/
        ├── ImplantaHubApplicationTests.java
        └── client/
            ├── ClientServiceTest.java
            └── ClientControllerTest.java

http/
└── clients.http

compose.yaml
pom.xml
README.md
```

A estrutura poderá evoluir conforme novos domínios e responsabilidades forem incorporados.

## Funcionalidades implementadas

### Health Check

Endpoint utilizado para verificar se a aplicação está respondendo.

```http
GET /api/health
```

Resposta:

```text
ImplantaHub API is running
```

### Cadastro de clientes

A API permite cadastrar clientes, verificando previamente se o CNPJ informado já existe.

```http
POST /api/clients
Content-Type: application/json
```

Exemplo de requisição:

```json
{
  "corporateName": "Empresa Exemplo Ltda",
  "tradeName": "Empresa Exemplo",
  "cnpj": "12345678000199",
  "email": "contato@exemplo.com",
  "phone": "11999999999"
}
```

Campos de entrada:

| Campo | Obrigatório | Regra |
|---|---|---|
| corporateName | Sim | Não pode estar em branco; máximo de 255 caracteres |
| tradeName | Não | Máximo de 255 caracteres |
| cnpj | Sim | Exatamente 14 dígitos numéricos |
| email | Sim | E-mail válido; máximo de 255 caracteres |
| phone | Não | Máximo de 20 caracteres |

O CNPJ deve ser enviado sem pontuação ou máscara.

Atualmente, a aplicação realiza apenas a validação estrutural do CNPJ, exigindo exatamente 14 dígitos de `0` a `9`. A validação matemática dos dígitos verificadores ainda não foi implementada.

O status inicial de um cliente é definido automaticamente como `ACTIVE` pelo `ClientService`. O consumidor da API não pode escolher esse valor durante o cadastro.

#### Cadastro realizado com sucesso

Uma criação bem-sucedida retorna:

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

Exemplo ilustrativo de resposta:

```json
{
  "id": 1,
  "corporateName": "Empresa Exemplo Ltda",
  "tradeName": "Empresa Exemplo",
  "cnpj": "12345678000199",
  "email": "contato@exemplo.com",
  "phone": "11999999999",
  "status": "ACTIVE",
  "createdAt": "2026-09-23T15:00:00Z",
  "updatedAt": "2026-09-23T15:00:00Z"
}
```

O identificador é gerado pelo PostgreSQL. Os campos `createdAt` e `updatedAt` registram os timestamps do cadastro.

Ainda não existe um endpoint de consulta por ID. Por isso, a resposta de criação não inclui o cabeçalho `Location`.

#### CNPJ duplicado

Antes de persistir um cliente, o `ClientService` consulta o Repository para verificar se o CNPJ já está cadastrado.

Quando a duplicidade é detectada, a aplicação lança `DuplicateCnpjException`, traduzida pelo `ClientExceptionHandler` para:

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json
```

Exemplo de resposta:

```json
{
  "type": "about:blank",
  "title": "Duplicate CNPJ",
  "status": 409,
  "detail": "A client with this CNPJ already exists."
}
```

A resposta não reproduz o CNPJ completo nem expõe informações internas de persistência.

A constraint `uq_clients_cnpj` no PostgreSQL continua sendo a garantia definitiva contra duplicidades, inclusive em operações concorrentes. A tradução específica de uma violação dessa constraint causada por concorrência ainda não foi implementada.

#### Dados inválidos

Os dados recebidos são validados com Jakarta Bean Validation, por meio da anotação `@Valid` no Controller.

Quando uma ou mais validações falham, a API retorna:

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json
```

Exemplo ilustrativo:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "errors": {
    "cnpj": [
      "must not be blank",
      "must match \"^[0-9]{14}$\""
    ],
    "email": [
      "must be a well-formed email address"
    ]
  }
}
```

As mensagens de validação podem variar conforme a implementação do validador.

A propriedade `errors` utiliza listas de mensagens para permitir que um mesmo campo apresente múltiplas violações sem perder informações.

Os valores rejeitados não são reproduzidos no corpo da resposta.

## Banco de dados

O ImplantaHub utiliza PostgreSQL 17, executado localmente por meio do Docker Compose.

Configuração de desenvolvimento:

```text
Database: implantahub
User: implantahub
Port: 5432
```

As alterações estruturais do banco são controladas pelo Flyway.

A primeira migration, `V1__create_clients_table.sql`, cria a tabela `clients` e suas constraints, incluindo:

- Chave primária com identificador gerado pelo banco;
- Razão social obrigatória;
- CNPJ obrigatório e único;
- Verificação estrutural do CNPJ;
- E-mail obrigatório;
- Status limitado a `ACTIVE` e `INACTIVE`;
- Timestamps de criação e atualização.

As migrations já aplicadas não devem ser alteradas. Futuras modificações estruturais serão realizadas por novas migrations versionadas.

O Hibernate está configurado com:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

Dessa forma, o Hibernate valida a compatibilidade das entidades com o schema existente, enquanto o Flyway permanece responsável pelas alterações estruturais do banco.

## Docker Compose

O arquivo `compose.yaml`, localizado na raiz do projeto, configura o PostgreSQL para o ambiente local de desenvolvimento.

Para iniciar o banco:

```powershell
docker compose up -d
```

Para verificar os containers:

```powershell
docker compose ps
```

Para visualizar os logs:

```powershell
docker compose logs -f postgres
```

Para interromper o ambiente preservando os dados:

```powershell
docker compose stop
```

Para remover os containers sem excluir o volume de dados:

```powershell
docker compose down
```

Para remover os containers e também o volume:

```powershell
docker compose down -v
```

> **Atenção:** `docker compose down -v` remove o volume do PostgreSQL, apagando os dados persistidos no ambiente local. Utilize esse comando somente quando realmente desejar recriar o banco.

## Executando a aplicação

### Pré-requisitos

Para executar o projeto localmente, são necessários:

- JDK 21 ou superior compatível com a configuração do projeto;
- Docker Desktop com Docker Compose;
- Maven Wrapper, já incluído no repositório.

O projeto utiliza Java 21 como versão alvo de compilação.

### Iniciar o ambiente

Primeiro, inicie o PostgreSQL:

```powershell
docker compose up -d
```

Em seguida, execute a aplicação pelo Maven Wrapper no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Também é possível executar diretamente a classe `ImplantaHubApplication` pelo IntelliJ IDEA.

Durante a inicialização, o Flyway verifica o histórico de migrations, e o Hibernate valida a estrutura das entidades em relação ao banco.

Por padrão, a aplicação fica disponível em:

```text
http://localhost:8080
```

Para verificar se a API está respondendo:

```text
http://localhost:8080/api/health
```

As credenciais presentes na configuração atual destinam-se exclusivamente ao ambiente local de desenvolvimento. Antes de disponibilizar a aplicação publicamente, será necessário configurar o gerenciamento adequado de segredos, autenticação e autorização.

## Testando a API manualmente

O repositório contém o arquivo:

```text
http/clients.http
```

Ele reúne exemplos de requisições para o endpoint de cadastro de clientes, incluindo:

1. Cadastro válido;
2. Tentativa de cadastro com CNPJ duplicado;
3. Requisição com dados inválidos.

No IntelliJ IDEA Ultimate, abra o arquivo e utilize os botões de execução ao lado de cada requisição.

Com a aplicação e o PostgreSQL em execução, os resultados esperados são:

| Cenário | Status HTTP |
|---|---|
| Cadastro bem-sucedido | 201 Created |
| CNPJ duplicado | 409 Conflict |
| Dados inválidos | 400 Bad Request |

**Importante:** após executar o primeiro cadastro, repetir a mesma requisição com o mesmo CNPJ deverá retornar `409 Conflict`. Para repetir o cenário de criação bem-sucedida, utilize um CNPJ de 14 dígitos ainda não cadastrado no ambiente local.

### Conferindo a persistência

É possível consultar os registros diretamente no PostgreSQL:

```powershell
docker compose exec postgres psql -U implantahub -d implantahub -c "SELECT id, corporate_name, cnpj, status, created_at, updated_at FROM clients;"
```

O fluxo de cadastro já foi validado manualmente, confirmando:

- Criação de cliente com resposta `201 Created`;
- Geração do identificador pelo banco;
- Persistência do cliente no PostgreSQL;
- Resposta `409 Conflict` para CNPJ duplicado;
- Ausência de registros duplicados após repetir o cadastro;
- Resposta `400 Bad Request` para dados inválidos.

Essa verificação foi realizada manualmente com a aplicação e o banco reais. Ela ainda não constitui uma suíte automatizada de testes de integração.

## Testes automatizados

O projeto utiliza JUnit 5, Mockito, AssertJ e MockMvc.

### Testes do ClientService

A classe `ClientServiceTest` executa testes unitários isolados, utilizando um mock de `ClientRepository`.

Os cenários implementados verificam:

- Criação de um cliente com CNPJ ainda não cadastrado;
- Atribuição automática do status `ACTIVE`;
- Preenchimento dos timestamps;
- Persistência por meio do Repository;
- Conversão para `ClientResponse`;
- Lançamento de `DuplicateCnpjException` quando o CNPJ já existe;
- Garantia de que `save()` não é chamado no cenário de duplicidade.

Esses testes não acessam o banco de dados.

### Testes do ClientController

A classe `ClientControllerTest` utiliza `@WebMvcTest`, MockMvc e um mock do `ClientService` para validar a camada HTTP.

Os cenários implementados verificam:

- Resposta `201 Created` para uma requisição válida;
- Resposta `409 Conflict` para CNPJ duplicado;
- Resposta `400 Bad Request` para dados inválidos;
- Serialização do `ClientResponse`;
- Utilização de `ProblemDetail` nos erros;
- Preservação de múltiplas mensagens de validação por campo;
- Ausência de chamada ao Service quando a validação falha.

Os testes utilizam a configuração MVC do Spring sem conectar ao PostgreSQL.

### Executando os testes

Para executar apenas os testes do Service:

```powershell
.\mvnw.cmd test "-Dtest=ClientServiceTest"
```

Para executar apenas os testes do Controller:

```powershell
.\mvnw.cmd test "-Dtest=ClientControllerTest"
```

Para executar toda a suíte:

```powershell
.\mvnw.cmd clean test
```

A suíte também contém um teste de inicialização do contexto da aplicação. Com a configuração atual, mantenha o PostgreSQL local disponível ao executar a suíte completa.

Os testes unitários e HTTP já foram executados com sucesso.

A implementação de testes de integração automatizados com Testcontainers está planejada para uma etapa futura.

## Build

Para compilar a aplicação:

```powershell
.\mvnw.cmd clean compile
```

Para executar os testes:

```powershell
.\mvnw.cmd clean test
```

Para gerar o pacote da aplicação:

```powershell
.\mvnw.cmd clean package
```

O artefato gerado ficará no diretório:

```text
target/
```

## Status do desenvolvimento

### Fundação

- [x] Projeto Spring Boot;
- [x] Java 21;
- [x] Maven Wrapper;
- [x] Spring Web MVC;
- [x] Bean Validation;
- [x] Health Check;
- [x] Git e GitHub;
- [x] Dependências de persistência;
- [x] Docker Compose;
- [x] PostgreSQL local;
- [x] Configuração do datasource;
- [x] Flyway;
- [x] Primeira migration aplicada.

### Domínio Client

- [x] Entidade Client;
- [x] Enum ClientStatus;
- [x] ClientRepository;
- [x] DTOs de entrada e saída;
- [x] Validação estrutural do CNPJ;
- [x] ClientService;
- [x] Tratamento de CNPJ duplicado na aplicação;
- [x] ClientController;
- [x] Endpoint POST /api/clients;
- [x] Tratamento HTTP de erros de negócio;
- [x] Tratamento HTTP de erros de validação;
- [x] Testes unitários do Service;
- [x] Testes HTTP do Controller;
- [x] Exemplos de requisições em clients.http;
- [x] Verificação manual de persistência no PostgreSQL.

### Próximas melhorias do domínio Client

- [ ] Consulta de clientes;
- [ ] Atualização de clientes;
- [ ] Desativação de clientes;
- [ ] Validação matemática do CNPJ;
- [ ] Tratamento específico da concorrência na criação;
- [ ] Testes de integração automatizados.

### Outros domínios e funcionalidades futuras

- [ ] Projetos de implantação;
- [ ] Etapas e checklists;
- [ ] Chamados de suporte;
- [ ] Controle de SLA;
- [ ] Usuários;
- [ ] Autenticação e autorização com Spring Security;
- [ ] Histórico e auditoria;
- [ ] OpenAPI / Swagger;
- [ ] CI/CD com GitHub Actions.

## Princípios do projeto

Algumas decisões adotadas durante o desenvolvimento:

- Organização por domínio/feature;
- Controllers sem regras de negócio;
- Entidades JPA não expostas diretamente pela API;
- Utilização de DTOs para comunicação HTTP;
- Schema do banco versionado pelo Flyway;
- Hibernate utilizado para validação do schema, não para gerá-lo;
- Regras de negócio concentradas na camada de serviço;
- Injeção de dependências via construtor;
- Fronteiras transacionais na camada de serviço;
- Exceções de negócio separadas da representação HTTP;
- Respostas de erro padronizadas com ProblemDetail;
- Testes unitários para regras relevantes;
- Testes específicos para a camada HTTP;
- Evitar dependências desnecessárias;
- Evitar overengineering;
- Implementação incremental;
- Monólito modular antes de considerar microsserviços.

## Autor

Desenvolvido como projeto de estudo e portfólio em Java e Spring Boot.
