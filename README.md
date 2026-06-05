<div align="center">

# 🐾 AdoTEC

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge&logo=github-actions)](https://github.com/JoaoRobrt/AdoTEC)
[![Version](https://img.shields.io/badge/version-0.2.0--SNAPSHOT-blue?style=for-the-badge)](https://github.com/JoaoRobrt/AdoTEC)
[![License](https://img.shields.io/badge/license-%E2%9A%A0%EF%B8%8F%20preencher-yellow?style=for-the-badge)](https://github.com/JoaoRobrt/AdoTEC)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

**API REST para gerenciamento de adoção de animais.**  
Conecta adotantes a animais disponíveis para adoção, gerenciando todo o fluxo — do cadastro do pet ao agendamento e resultado da visita.

</div>

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Pré-requisitos](#pré-requisitos)
- [Guia de Instalação](#guia-de-instalação)
- [Exemplos de Uso](#exemplos-de-uso)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Rodando os Testes](#rodando-os-testes)
- [Documentação da API](#documentação-da-api)
- [Arquitetura](#arquitetura)

---

## Sobre o Projeto

**AdoTEC** é uma API REST desenvolvida com **Spring Boot 4** que centraliza o processo de adoção de animais de abrigos. A plataforma resolve os principais pontos de atrito nesse processo:

- **Para adotantes**: listagem de pets disponíveis com filtros avançados (porte, espécie, faixa de idade, sexo), ordenação, agendamento de visitas em horários pré-definidos e acompanhamento do status de cada consulta.
- **Para funcionários**: gerenciamento do cadastro de animais, upload de fotos via Cloudinary e registro do resultado final de cada visita.
- **Para administradores**: controle total sobre agendamentos (com filtros por status, funcionário e não atribuídos), atribuição de funcionários às visitas, painel de métricas operacionais (dashboard), gestão de funcionários (CRUD) e visualização consolidada de toda a operação.

A autenticação é baseada em **JWT (cookie HttpOnly)** e o acesso a cada recurso é controlado por papéis (`ROLE_ADMIN`, `ROLE_EMPLOYEE`, `ROLE_ADOPTER`). O endpoint de login é protegido contra ataques de força bruta com **rate limiting distribuído via Bucket4j + Redis**. O cache de pets em destaque é gerenciado com **Redis + Jackson 3.x**.

---

## Funcionalidades

### 🔐 Autenticação (`/auth`)
- `POST /auth/register` — Cadastro de novo usuário (perfil Adotante)
- `POST /auth/login` — Autenticação com e-mail e senha; retorna JWT via cookie HttpOnly (protegido por rate limit)
- `GET /auth/me` — Recupera os dados do usuário autenticado

### 🐶 Pets (`/pets`)
- `GET /pets` — Listagem paginada de pets disponíveis para adoção com filtros avançados:
  - `petSize` — Porte (`SMALL`, `MEDIUM`, `BIG`)
  - `species` — Espécie (ex: `Cachorro`, `Gato`, `Coelho`)
  - `minAge` / `maxAge` — Faixa de idade em meses
  - `gender` — Sexo (`MALE`, `FEMALE`)
  - `name` — Busca por nome (parcial, case-insensitive)
  - `sort` — Ordenação (ex: `createdAt,desc`, `petName,asc`, `ageInMonths,asc`)
- `GET /pets/{id}` — Detalhes de um pet específico
- `GET /pets/featured` — Pets em destaque para a página inicial (com cache Redis)
- `POST /pets` — Cadastro de novo pet _(ADMIN / EMPLOYEE)_
- `PUT /pets/{id}` — Atualização dos dados de um pet _(ADMIN / EMPLOYEE)_
- `DELETE /pets/{id}` — Remoção lógica (soft-delete) de um pet _(ADMIN / EMPLOYEE)_

### 📸 Fotos de Pets (`/pets/{petId}/photos`)
- `POST /pets/{petId}/photos` — Upload de foto via multipart/form-data para o Cloudinary _(ADMIN / EMPLOYEE)_
- `GET /pets/{petId}/photos` — Listagem de todas as fotos de um pet (público)
- `DELETE /pets/{petId}/photos/{photoId}` — Exclusão de uma foto específica _(ADMIN / EMPLOYEE)_
- `PATCH /pets/{petId}/photos/{photoId}/primary` — Define foto como principal _(ADMIN / EMPLOYEE)_

### 📅 Agendamentos (`/appointments`)
- `POST /appointments` — Agendamento de visita por um adotante _(ADOPTER)_
- `GET /appointments` — Listagem paginada de todos os agendamentos com filtros _(ADMIN)_:
  - `status` — Filtro por status (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELED`)
  - `employeeId` — Filtro por funcionário atribuído
  - `unassigned` — Exibir apenas agendamentos sem funcionário atribuído
  - `showCanceled` — Incluir/excluir cancelados (padrão: `true`)
- `GET /appointments/{id}` — Detalhes de um agendamento específico _(ADMIN / EMPLOYEE / dono do agendamento)_
- `GET /appointments/me` — Agendamentos do usuário logado com filtros por status _(ADOPTER / EMPLOYEE)_
- `PATCH /appointments/{id}/assign/{employeeId}` — Atribui um funcionário ao agendamento _(ADMIN)_
- `PATCH /appointments/{id}/result` — Registra o resultado da visita (`APPROVED` ou `REJECTED`) _(ADMIN / EMPLOYEE)_
- `PATCH /appointments/{id}/cancel` — Cancela um agendamento _(ADOPTER dono)_

### 📊 Dashboard (`/dashboard`)
- `GET /dashboard/unassigned-appointments` — Agendamentos pendentes de atribuição, paginados _(ADMIN)_

### 👥 Funcionários (`/employees`)
- `GET /employees` — Listagem de todos os funcionários _(ADMIN)_
- `GET /employees/{id}` — Detalhes de um funcionário _(ADMIN)_
- `POST /employees` — Cadastro de novo funcionário ou administrador _(ADMIN)_
- `PUT /employees/{id}` — Atualização de nome e e-mail _(ADMIN)_
- `PATCH /employees/{id}/toggle-active` — Ativa/desativa um funcionário (soft-delete) _(ADMIN)_

### 🕐 Horários Disponíveis (`/timeslots`)
- `GET /timeslots?date=YYYY-MM-DD` — Horários disponíveis para uma data específica
- `GET /timeslots?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` — Horários disponíveis em um intervalo de datas

### 📊 Monitoramento (`/actuator`)
- `GET /actuator/health` — Status de saúde da aplicação (detalhado apenas no perfil `dev`)

---

## Pré-requisitos

Antes de instalar e executar o projeto, certifique-se de que as seguintes dependências estão instaladas e configuradas:

| Ferramenta      | Versão mínima | Observações                                        |
|-----------------|---------------|-----------------------------------------------------|
| **JDK**         | 21            | Recomendado: Eclipse Temurin 21 ou OpenJDK 21      |
| **Maven**       | 3.9+          | Ou use o wrapper `./mvnw` incluso no projeto       |
| **MySQL**       | 8.0+          | Banco de dados principal da aplicação              |
| **Redis**       | 7.0+          | Necessário para rate limiting e cache de pets      |
| **Git**         | 2.x           | Para clonar o repositório                          |

> **Conta Cloudinary**: Para o módulo de upload de fotos funcionar, é necessária uma conta gratuita no [Cloudinary](https://cloudinary.com/). As credenciais são configuradas via variáveis de ambiente.

---

## Guia de Instalação

### 1. Clonar o repositório

```bash
git clone git@github.com:JoaoRobrt/AdoTEC.git
cd AdoTEC
```

### 2. Configurar o banco de dados MySQL

Crie o banco de dados e um usuário dedicado:

```sql
CREATE DATABASE adotec_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'adotec_user'@'localhost' IDENTIFIED BY 'sua_senha_aqui';
GRANT ALL PRIVILEGES ON adotec_db.* TO 'adotec_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Iniciar o Redis

Certifique-se de que o Redis está em execução na porta padrão (`6379`):

```bash
# Usando Docker (recomendado para desenvolvimento)
docker run -d --name adotec-redis -p 6379:6379 redis:7-alpine

# Ou inicie o serviço local (Linux/macOS)
redis-server
```

### 4. Configurar as variáveis de ambiente

Copie o arquivo de exemplo e preencha as variáveis:

```bash
cp .env.example .env
```

Edite o arquivo `.env` criado com os valores reais (veja a seção [Variáveis de Ambiente](#variáveis-de-ambiente) para detalhes).

### 5. Executar em modo de desenvolvimento

```bash
# Usando o Maven Wrapper (recomendado)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

O Spring Boot Devtools está configurado como dependência de runtime — alterações no código reiniciam a aplicação automaticamente.

A API estará disponível em: `http://localhost:8080`

> **Dados de demonstração**: No perfil `dev`, o `DevDataSeeder` cria automaticamente usuários padrão (admin, funcionário, adotante) e 12 pets de demonstração com fotos, permitindo testar a aplicação imediatamente.

### 6. Executar em produção (JAR)

```bash
# Gerar o artefato
./mvnw clean package -DskipTests

# Executar o JAR com o perfil de produção
java -jar target/AdoTEC-0.2.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Exemplos de Uso

> Todos os exemplos abaixo assumem que a API está em execução em `http://localhost:8080`.  
> As rotas protegidas exigem que o cookie JWT (obtido no login) seja enviado automaticamente pelo cliente HTTP.

---

### Exemplo 1 — Autenticar usuário

**`POST /auth/login`**

```bash
curl -c cookies.txt -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "adotante@email.com",
    "password": "minhasenha123"
  }'
```

**Resposta esperada (`200 OK`):**

```json
{
  "status": "success",
  "message": "User authenticated successfully",
  "data": {
    "id": 1,
    "name": "João Silva",
    "email": "adotante@email.com",
    "roles": ["ROLE_ADOPTER"]
  }
}
```

> O token JWT é retornado como cookie HttpOnly (`JWT_COOKIE_NAME`) e enviado automaticamente nas requisições subsequentes.

---

### Exemplo 2 — Listar pets com filtros avançados

**`GET /pets?petSize=SMALL&species=Cachorro&gender=FEMALE&minAge=0&maxAge=12&sort=petName,asc`**

```bash
curl -X GET "http://localhost:8080/pets?petSize=SMALL&species=Cachorro&gender=FEMALE&minAge=0&maxAge=12&sort=petName,asc" \
  -H "Accept: application/json"
```

**Resposta esperada (`200 OK`):**

```json
{
  "status": "success",
  "message": "Pets retrieved successfully",
  "data": {
    "content": [
      {
        "petId": 4,
        "petName": "Mel",
        "species": "Cachorro",
        "description": "Mel é uma filhotinha meiga e sapeca. Perfeita para apartamento.",
        "ageInMonths": 8,
        "size": "SMALL",
        "gender": "FEMALE",
        "isAvailableForAdoption": true,
        "photos": [
          {
            "photoId": 12,
            "url": "https://images.unsplash.com/photo-1560807707-8cc77767d783?w=600&h=600&fit=crop",
            "isPrimary": true
          }
        ],
        "createdAt": "2026-06-01T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 12,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
}
```

---

### Exemplo 3 — Agendar uma visita

**`POST /appointments`** _(requer cookie de sessão com papel `ADOPTER`)_

```bash
curl -b cookies.txt -X POST http://localhost:8080/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "petId": 5,
    "timeSlotId": 3
  }'
```

**Resposta esperada (`201 Created`):**

```json
{
  "status": "success",
  "message": "Appointment scheduled successfully",
  "data": {
    "appointmentId": 42,
    "status": "PENDING",
    "petName": "Rex",
    "adopterName": "João Silva",
    "timeSlot": {
      "date": "2026-06-10",
      "startTime": "09:00:00",
      "endTime": "10:00:00"
    },
    "employeeName": null,
    "adoptionResult": null,
    "notes": null,
    "createdAt": "2026-05-25T16:00:00Z"
  }
}
```

---

### Exemplo 4 — Consultar horários disponíveis por data

**`GET /timeslots?date=2026-06-10`**

```bash
curl -X GET "http://localhost:8080/timeslots?date=2026-06-10" \
  -H "Accept: application/json"
```

**Resposta esperada (`200 OK`):**

```json
{
  "status": "success",
  "message": "Time slots retrieved successfully",
  "data": [
    {
      "timeSlotId": 3,
      "date": "2026-06-10",
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "maxAppointments": 5,
      "currentAppointments": 2,
      "available": true
    },
    {
      "timeSlotId": 4,
      "date": "2026-06-10",
      "startTime": "14:00:00",
      "endTime": "15:00:00",
      "maxAppointments": 5,
      "currentAppointments": 5,
      "available": false
    }
  ]
}
```

---

### Exemplo 5 — Registrar resultado de uma visita

**`PATCH /appointments/42/result`** _(requer papel `ADMIN` ou `EMPLOYEE`)_

```bash
curl -b cookies.txt -X PATCH http://localhost:8080/appointments/42/result \
  -H "Content-Type: application/json" \
  -d '{
    "adoptionResult": "APPROVED",
    "notes": "Adotante demonstrou boas condições para receber o animal."
  }'
```

**Resposta esperada (`200 OK`):**

```json
{
  "status": "success",
  "message": "Appointment result registered successfully",
  "data": {
    "appointmentId": 42,
    "status": "COMPLETED",
    "adoptionResult": "APPROVED",
    "notes": "Adotante demonstrou boas condições para receber o animal."
  }
}
```

---

## Variáveis de Ambiente

Todas as variáveis abaixo devem ser definidas no arquivo `.env` na raiz do projeto. O arquivo `.env.example` fornece um template inicial.

| Variável                 | Descrição                                                           | Obrigatória |
|--------------------------|---------------------------------------------------------------------|:-----------:|
| `DB_URL`                 | URL JDBC de conexão ao MySQL (ex: `jdbc:mysql://localhost:3306/adotec_db`) | ✅ Sim |
| `DB_USERNAME`            | Usuário do banco de dados MySQL                                     | ✅ Sim      |
| `DB_PASSWORD`            | Senha do usuário do banco de dados MySQL                            | ✅ Sim      |
| `JWT_SECRET`             | Chave secreta para assinatura dos tokens JWT (mínimo 256 bits / 32 caracteres) | ✅ Sim |
| `JWT_EXPIRATION_MS`      | Tempo de expiração do token JWT em milissegundos (ex: `86400000` = 24h) | ✅ Sim |
| `JWT_COOKIE_NAME`        | Nome do cookie HttpOnly que armazenará o token (ex: `adotec-jwt`)  | ✅ Sim      |
| `CLOUDINARY_CLOUD_NAME`  | Nome da cloud do Cloudinary (encontrado no dashboard)              | ✅ Sim      |
| `CLOUDINARY_API_KEY`     | Chave de API do Cloudinary                                          | ✅ Sim      |
| `CLOUDINARY_API_SECRET`  | Segredo de API do Cloudinary                                        | ✅ Sim      |

**Exemplo de `.env` preenchido:**

```dotenv
# Banco de dados
DB_URL=jdbc:mysql://localhost:3306/adotec_db?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=adotec_user
DB_PASSWORD=adotec_pass_segura

# JWT
JWT_SECRET=minha-chave-super-secreta-com-256bits-minimo-aqui
JWT_EXPIRATION_MS=86400000
JWT_COOKIE_NAME=adotec-jwt

# Cloudinary
CLOUDINARY_CLOUD_NAME=meu-cloud-name
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abc123def456ghi789jkl012mno345
```

> ⚠️ **Importante:** Nunca versione o arquivo `.env` com valores reais. Ele já está no `.gitignore` do projeto.

---

## Rodando os Testes

O projeto utiliza **JUnit 5** e **Spring Security Test** como frameworks de testes. O banco de dados H2 (in-memory) é utilizado automaticamente durante a execução dos testes, sem necessidade de conexão externa.

### Executar todos os testes

```bash
./mvnw test
```

### Executar os testes de um serviço específico

```bash
# Testes do PetService
./mvnw test -Dtest=PetServiceTest

# Testes do AppointmentService
./mvnw test -Dtest=AppointmentServiceTest

# Testes do AuthService
./mvnw test -Dtest=AuthServiceTest

# Testes do TimeSlotService
./mvnw test -Dtest=TimeSlotServiceTest

# Testes do UserService
./mvnw test -Dtest=UserServiceTest
```

### Gerar relatório de cobertura (Surefire)

```bash
./mvnw test surefire-report:report
# O relatório HTML estará em: target/site/surefire-report.html
```

---

## Documentação da API

A documentação interativa da API é gerada automaticamente pelo **SpringDoc OpenAPI (Swagger UI)** e está disponível após iniciar a aplicação:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Arquitetura

O projeto segue os princípios de **Clean Architecture** e **separação de responsabilidades**, com a seguinte estrutura de pacotes:

```
com.joao.adotec/
├── config/          # Configurações (CacheConfig Redis, bootstrap de dados dev)
├── controllers/     # Camada de apresentação (endpoints REST)
│   ├── AuthController, PetController, PetPhotoController
│   ├── AppointmentController, TimeSlotController
│   ├── DashboardController, EmployeeController
├── dto/             # Data Transfer Objects (request, response, commons)
├── enums/           # Enumerações (AppRole, AppointmentStatus, AdoptionResult, PetSize, PetGender)
├── exceptions/      # Exceções customizadas e handler global
├── mappers/         # Conversões Entity ↔ DTO via MapStruct
├── models/          # Entidades JPA (Pet, Appointment, TimeSlot, User, Role, PetPhoto)
├── repositories/    # Interfaces Spring Data JPA
├── security/        # Configuração do Spring Security, filtros JWT e Rate Limiting
└── services/        # Regras de negócio
```

### Principais tecnologias

| Tecnologia                         | Uso                                                  |
|------------------------------------|------------------------------------------------------|
| Spring Boot 4.0.4                  | Framework principal                                  |
| Spring Security + JWT (JJWT 0.12.6)| Autenticação e autorização                          |
| Spring Data JPA + Hibernate        | Persistência com MySQL                               |
| Spring Data Redis + Jackson 3.x    | Cache de pets em destaque (serialização JSON)        |
| MapStruct 1.6.3                    | Mapeamento Entity ↔ DTO via geração de código        |
| Bucket4j 8.16.1 + Redis            | Rate limiting distribuído no endpoint de login       |
| Cloudinary SDK 1.39.0              | Upload e gerenciamento de fotos de pets              |
| SpringDoc OpenAPI 3.0.2            | Documentação automática da API (Swagger UI)          |
| Spring Boot Actuator               | Monitoramento de saúde da aplicação                  |
| Lombok                             | Redução de boilerplate (getters, construtores, etc.) |
| H2 Database                        | Banco de dados in-memory para testes                 |

---

<div align="center">
  <sub>Desenvolvido como Projeto Integrador · AdoTEC · 2026</sub>
</div>
