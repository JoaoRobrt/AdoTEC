# AdoTEC — Arquitetura, Regras de Negócio e Bugs Conhecidos

## 1. Visão Geral

**Sistema:** Backend REST API — Adoção do Centro de Zoonoses de Campina Grande, PB.
**Fluxo central:** `Pet disponível → Adotante escolhe TimeSlot → Cria Appointment → Funcionário registra resultado`

### Atores

| Ator | Responsabilidade | Role |
| :--- | :--- | :--- |
| Adotante | Navega no catálogo, agenda visita, cancela seu próprio agendamento | `ROLE_ADOPTER` |
| Funcionário | Atende a visita e registra o resultado | `ROLE_EMPLOYEE` |
| Administrador | Gerencia pets, horários, todos os agendamentos, atribui funcionários | `ROLE_ADMIN` |

---

## 2. Arquitetura em Camadas

```
Controller → Service → Repository → Database
```

| Camada | Responsabilidade | Proibido |
| :--- | :--- | :--- |
| Controller | HTTP, `@Valid`, `@PreAuthorize`, delegar ao service, devolver `ApiResponse<T>` | Lógica de negócio, acesso a repositório |
| Service | Regras de negócio, transações, exceções de domínio | Conhecer HTTP ou Swagger |
| Mapper (MapStruct) | Converter Entity ↔ DTO | Acessar repositório ou banco |
| Repository | Consultas JPA | Regras de negócio |
| Model (Entity) | Estado persistido | Expor-se diretamente na API |
| Security | JWT, autorização por role, filtros | Misturar com regras de domínio |
| Exception Handler | Traduzir exceções em respostas HTTP | Lógica de domínio |

**Envelope de resposta obrigatório:**
```json
{ "timestamp": "...", "message": "...", "data": { ... } }
```

**Envelope de paginação:**
```json
{
  "data": {
    "content": [ ... ],
    "pagination": { "number": 0, "size": 10, "totalElements": 42, "totalPages": 5, "first": true, "last": false }
  }
}
```

---

## 3. Modelagem de Dados

### Entidades

**User (`tb_users`):** `userId`, `name`, `email` (UNIQUE), `password` (BCrypt), `createdAt`, `isActive`, `roles` (@ManyToMany EAGER).

**Role (`tb_roles`):** `roleId`, `roleName` — enum `AppRole`: `ROLE_ADOPTER` / `ROLE_EMPLOYEE` / `ROLE_ADMIN`.

**Pet (`tb_pets`):** `petId`, `petName`, `species` (texto livre), `description`, `ageInMonths`, `size` (enum `PetSize`: SMALL/MEDIUM/BIG), `photoUrl`, `isAvailableForAdoption`, `isActive` (soft-delete), `createdAt`.

**TimeSlot (`tb_time_slots`):** `timeSlotId`, `date`, `startTime`, `endTime`, `maxAppointments`.

**Appointment (`tb_appointments`):** `appointmentId`, `status` (enum: PENDING/CONFIRMED/COMPLETED/CANCELED), `createdAt`, `adoptionResult` (APPROVED/REJECTED, nullable), `notes`, `adopter_id` (NOT NULL), `employee_id` (nullable), `pet_id`, `time_slot_id`.

### Relacionamentos

| Origem → Destino | Tipo | Fetch |
| :--- | :--- | :--- |
| User ↔ Role | @ManyToMany (`tb_user_roles`) | EAGER |
| User → Appointment (adopter/employee) | @OneToMany ↔ @ManyToOne | LAZY |
| Pet → Appointment | @OneToMany ↔ @ManyToOne | LAZY |
| TimeSlot → Appointment | @OneToMany ↔ @ManyToOne | LAZY |

### Decisões de persistência

- Enums: `@Enumerated(EnumType.STRING)` em todos — obrigatório.
- Identidade: `GenerationType.IDENTITY`.
- Timestamps: `@CreationTimestamp` JVM-side.
- Soft-delete em `Pet`: flag `isActive`, filtro explícito nos repositórios.
- DDL: `ddl-auto: update` (⚠ não usar em produção — bug BUG-12).

---

## 4. Regras de Negócio

### Auth (`AuthService`)

| ID | Regra |
| :--- | :--- |
| RB-AUTH-01 | E-mail único — 409 se já existir |
| RB-AUTH-02 | Signup cria usuário com única role `ROLE_ADOPTER` |
| RB-AUTH-03 | Após signup, sistema autentica automaticamente (auto-login) |
| RB-AUTH-04 | Senha: mínimo 6, máximo 40 caracteres; e-mail no formato `@Email` |
| RB-AUTH-05 | Senha armazenada com BCrypt |

### Pets (`PetService`)

| ID | Regra |
| :--- | :--- |
| RB-PET-01 | Listagem pública: apenas `isActive=true AND isAvailableForAdoption=true`, com filtro por `size` e `name` (case-insensitive) |
| RB-PET-02 | `GET /pets/{id}`: 404 se inativo ou indisponível |
| RB-PET-03 | `createPet` força `isAvailableForAdoption=true`, ignorando valor enviado |
| RB-PET-04 | `updatePet` ignora `petId`, `isAvailableForAdoption` e `createdAt` (mapper) |
| RB-PET-05 | Soft-delete: apenas marca `isActive=false`; histórico preservado |
| RB-PET-06 | Criar/atualizar/deletar: exige `ADMIN` ou `EMPLOYEE` |

### TimeSlots (`TimeSlotService`)

| ID | Regra |
| :--- | :--- |
| RB-TS-01 | Listagem retorna apenas slots com capacidade restante |
| RB-TS-02 | Filtro por `?date=` ou `?startDate=&endDate=` |
| RB-TS-03 | Endpoint público |

### Agendamentos (`AppointmentService`)

| ID | Regra |
| :--- | :--- |
| RB-APP-01 | Data do TimeSlot não pode ser anterior a hoje |
| RB-APP-02 | `endTime > startTime` no TimeSlot |
| RB-APP-03 | Adotante não pode ter dois agendamentos no mesmo TimeSlot |
| RB-APP-04 | Capacidade do TimeSlot respeitada (`countByTimeSlot < maxAppointments`) |
| RB-APP-05 | Todo Appointment criado com status `PENDING` |
| RB-APP-06 | Atribuição de funcionário exige `ROLE_EMPLOYEE` |
| RB-APP-07 | Registrar resultado muda status para `COMPLETED`; não permite re-registro |
| RB-APP-08 | `APPROVED` → Pet marcado como `isAvailableForAdoption=false` |
| RB-APP-09 | Cancelamento: apenas pelo próprio adopter dono (⚠ ver BUG-08) |
| RB-APP-10 | Não cancela appointment `COMPLETED` |

### Autorização

| ID | Regra |
| :--- | :--- |
| RB-AZ-01 | `POST/PUT/DELETE /pets/**`: exige `ADMIN` ou `EMPLOYEE` |
| RB-AZ-02 | `/auth/**`, `GET /pets`, `GET /pets/**`, `GET /timeslots`, Swagger: públicos |
| RB-AZ-03 | Demais endpoints: exigem autenticação |

---

## 5. Endpoints

Base path: raiz (sem prefixo `/api`). Autenticação: `Authorization: Bearer <token>`.

| # | Método | Rota | Auth | Status codes |
| :--- | :--- | :--- | :--- | :--- |
| 1 | POST | `/auth/login` | público | 200, 401, 400 |
| 2 | POST | `/auth/register` | público | 201, 400, 409 |
| 3 | GET | `/auth/me` | autenticado | 200, 401 |
| 4 | GET | `/pets` | público | 200 |
| 5 | GET | `/pets/{id}` | público | 200, 404 |
| 6 | POST | `/pets` | ADMIN/EMPLOYEE | 201, 400, 403 |
| 7 | PUT | `/pets/{id}` | ADMIN/EMPLOYEE | 200, 400, 403, 404 |
| 8 | DELETE | `/pets/{id}` | ADMIN/EMPLOYEE | 204, 403, 404 |
| 9 | GET | `/timeslots` | público | 200, 400 |
| 10 | POST | `/appointments` | ADOPTER | 201, 400, 403, 404, 409 |
| 11 | GET | `/appointments/{id}` | autenticado | 200, 401, 404 |
| 12 | GET | `/appointments/me` | ADOPTER/EMPLOYEE | 200, 403 |
| 13 | GET | `/appointments` | ADMIN | 200, 403 |
| 14 | PATCH | `/appointments/{id}/assign/{employeeId}` | ADMIN | 200, 403, 404 |
| 15 | PATCH | `/appointments/{id}/result` | ADMIN/EMPLOYEE | 200, 400, 403, 404 |
| 16 | PATCH | `/appointments/{id}/cancel` | ADOPTER | 200, 403, 404 |
| 17 | GET | `/employees` | EMPLOYEE (⚠ BUG-10) | 200, 403 |

---

## 6. Segurança

### Como a autenticação funciona (de fato)

O código possui `generateJwtCookie()` e `getJwtFromCookies()` em `JwtUtils`, mas **nenhum controller os usa**. O `AuthTokenFilter` lê **exclusivamente** o header `Authorization: Bearer`. O código de cookie é morto.

**Regra prática:** sempre `Authorization: Bearer <token>`. Nunca cookie.

### Configuração

| Item | Valor |
| :--- | :--- |
| Hash de senha | BCrypt (custo 10) |
| Session policy | STATELESS |
| CSRF | Desabilitado |
| CORS origens | `localhost:3000`, `localhost:5173` (hardcoded — BUG-11) |
| JWT algoritmo | HMAC-SHA |
| JWT secret | `${JWT_SECRET}` (Base64, mínimo 256 bits) |

### ⚠ Hot path sem cache

Cada requisição autenticada dispara `userDetailsService.loadUserByUsername()` → hit no banco. Sem cache implementado (ver BUG-13).

---

## 7. Tratamento de Erros

### Hierarquia de exceções

```
RuntimeException
├── ApiException          → 500/customizado
└── DomainException
    ├── BusinessException → 409 CONFLICT
    └── ResourceNotFoundException → 404 NOT_FOUND
```

### Formato ProblemDetail (RFC 7807)

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/auth/register",
  "timestamp": "...",
  "errors": { "campo": "mensagem" }
}
```

---

## 8. Bugs Conhecidos

> **ATENÇÃO:** Não "corrija" estes itens sem instrução explícita. São problemas documentados aguardando tratamento intencional. Corrigir sem contexto pode introduzir regressões.

### 🔴 Críticos

| ID | Problema |
| :--- | :--- |
| BUG-01 | Seed cria `admin@adotec.com/admin123` sem `@Profile("dev")` — executa em produção |
| BUG-02 | Sem rate limiting em `/auth/login` — credential stuffing trivial |
| BUG-03 | IDOR em `GET /appointments/{id}` — qualquer autenticado lê appointment de outro |
| BUG-04 | Race condition em `createAppointment` — TOCTOU, overbooking possível |
| BUG-05 | Falta `@Version` e `UNIQUE(adopter_id, time_slot_id)` — sem locking otimista |

### 🟠 Altos

| ID | Problema |
| :--- | :--- |
| BUG-06 | `countByTimeSlot` inclui appointments `CANCELED` — consomem capacidade indevidamente |
| BUG-07 | `GET /timeslots` filtra capacidade em memória incluindo cancelados — resultado incorreto |
| BUG-08 | Admin não consegue cancelar agendamento — restrito ao adopter dono |
| BUG-09 | Qualquer `EMPLOYEE` pode registrar resultado de qualquer visita (não verifica o atribuído) |
| BUG-10 | `GET /employees` exige `ROLE_EMPLOYEE` em vez de `ROLE_ADMIN` |
| BUG-11 | CORS hardcoded para `localhost:3000` e `localhost:5173` |
| BUG-12 | `ddl-auto: update` em produção — risco de drift de schema |
| BUG-13 | `UserDetails` carregado do banco a cada requisição — sem cache |
| BUG-14 | `DataIntegrityViolationException` cai em 500 — deveria ser 409 |

### 🟡 Médios

| ID | Problema |
| :--- | :--- |
| BUG-15 | `java-dotenv` impede deploy em Docker/k8s — mensagem de erro com path do dev original |
| BUG-16 | `spring-boot-starter-mail` declarado mas sem uso |
| BUG-17 | Enum `CONFIRMED` morto — nenhuma transição o atinge |
| BUG-18 | Colisão de parâmetro `?size=` em `GET /pets` (filtro de tamanho vs `Pageable.size`) |
| BUG-19 | `GET /timeslots` sem paginação e sem limite de range — DoS potencial |
| BUG-20 | `GET /appointments/{id}` sem `@PreAuthorize` granular |
| BUG-21 | Dois formatos de erro coexistem: `ProblemDetail` no handler vs JSON manual no 401/403 |
| BUG-22 | `@Setter` global nas entidades JPA — mutação irrestrita |

---

## 9. Padrões de Código

### Faça sempre

- DTOs como records onde há imutabilidade.
- Constructor injection via `@RequiredArgsConstructor`.
- `@Transactional(readOnly=true)` em leitura; `@Transactional` em escrita.
- `@PreAuthorize` declarativo no controller.
- MapStruct com `componentModel = "spring"`.
- Exceções de domínio lançadas no service.

### Nunca faça

- Retornar entidade JPA de controller ou endpoint.
- Lógica de negócio no controller ou repositório.
- `@AllArgsConstructor` em entidades JPA.
- Imports fully qualified inline.
- `Mappers.getMapper(XMapper.class)` — usar injeção Spring.

---

## 10. Ambiente e Configuração

### Variáveis obrigatórias (`.env` na raiz)

```env
DB_URL=jdbc:mysql://localhost:3306/adotec
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...          # Base64, mínimo 256 bits
JWT_EXPIRATION_MS=86400000
JWT_COOKIE_NAME=adotec_jwt
```

### Seed de dados (⚠ todos os perfis)

| Usuário | Senha | Roles |
| :--- | :--- | :--- |
| `admin@adotec.com` | `admin123` | ADMIN + EMPLOYEE + ADOPTER |
| `employee@adotec.com` | `employee123` | EMPLOYEE + ADOPTER |
| `adopter@adotec.com` | `adopter123` | ADOPTER |

---

## 11. Cobertura de Testes

| Camada | Cobertura |
| :--- | :--- |
| Controllers | 🟢 5/5 com teste |
| Services | 🔴 Zero — nenhum teste unitário |
| Repositories | 🔴 Zero — sem `@DataJpaTest` |
| Mappers | 🔴 Zero |
| Security filter | 🟡 Apenas `SecurityResponseTest` |

**Atenção:** testes de controller usam `MockMvc.standaloneSetup` — não executam filtro JWT nem `@PreAuthorize`. Autorização real não é testada.

---

*Referência gerada em 2026-05-13 a partir de https://github.com/JoaoRobrt/AdoTEC*
