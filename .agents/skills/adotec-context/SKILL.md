# AdoTEC — Contexto do Projeto

## Descrição
Use esta skill em TODA tarefa relacionada ao projeto AdoTEC — seja implementar uma feature, corrigir um bug, criar um teste, refatorar código ou responder perguntas sobre a arquitetura. Esta skill deve ser carregada antes de qualquer ação no repositório.

## Instruções Obrigatórias

Antes de qualquer implementação, leia o arquivo `references/arquitetura.md` completo.
Ele contém as regras de negócio, bugs conhecidos, padrões e decisões arquiteturais do projeto.

### Nunca faça sem consultar a referência

- Nunca retorne uma entidade JPA diretamente de um controller ou endpoint.
- Nunca coloque lógica de negócio no controller ou no repositório.
- Nunca use `@Autowired` em campo — sempre use constructor injection via `@RequiredArgsConstructor`.
- Nunca instancie mappers com `Mappers.getMapper(XMapper.class)` — use injeção Spring.
- Nunca ignore a seção **Bugs Conhecidos** antes de implementar algo que toque em agendamentos, capacidade de TimeSlot, cancelamento, ou autenticação.

### Nunca confie no frontend

Todo dado que chega ao backend deve ser tratado como potencialmente malicioso, independente da origem. O frontend é um cliente não confiável — qualquer validação feita apenas no cliente pode ser bypassada.

- **Sempre revalide no backend** qualquer dado recebido, mesmo que o frontend já valide no formulário. Use `@Valid` + Bean Validation nos DTOs de entrada (`@NotBlank`, `@Size`, `@Email`, `@NotNull`).
- **Nunca confie em IDs enviados pelo cliente** para determinar o dono de um recurso. O `adopterId`, `userId` ou qualquer ID de propriedade deve ser extraído do JWT autenticado (`Authentication` / `UserDetailsImpl`), nunca do body ou query param da requisição.
- **Nunca confie em roles ou permissões enviadas pelo cliente.** A autorização é sempre determinada pelo JWT validado pelo `AuthTokenFilter` e pelas anotações `@PreAuthorize` no backend — nunca por um campo `role` vindo da requisição.
- **Nunca confie em status de entidades enviados pelo cliente.** O status inicial de um `Appointment` é sempre `PENDING` (RB-APP-05), definido pelo backend — ignorar qualquer campo `status` no request de criação.
- **Nunca confie em flags de disponibilidade enviadas pelo cliente.** `isAvailableForAdoption` em `createPet` é sempre forçado para `true` pelo backend (RB-PET-03), independente do valor enviado.
- **Revalide regras de negócio no service**, mesmo que o frontend informe que uma condição já foi verificada. Exemplos: capacidade do TimeSlot, data no passado, duplicidade de agendamento — tudo é checado no `AppointmentService`.
- **Trate todos os campos de texto como potencialmente perigosos.** Aplique `@Size` para limitar tamanho e considere sanitização em campos como `notes` e `description` que são armazenados e podem ser exibidos.

### Sempre faça

- Use DTOs para entrada e saída de qualquer endpoint. Conversão via MapStruct.
- Retorne sempre via `ApiResponse<T>` ou `ResponseEntity<ApiResponse<T>>`.
- Use `@Transactional(readOnly = true)` em métodos de leitura e `@Transactional` em escrita.
- Declare `@PreAuthorize` no controller, próximo ao endpoint.
- Lance exceções de domínio (`BusinessException`, `ResourceNotFoundException`) no service, nunca no controller.
- Siga o fluxo: Controller → Service → Repository → Database.

### Padrão de nomenclatura obrigatório

Sufixos: `*Controller`, `*Service`, `*Repository`, `*RequestDTO`, `*ResponseDTO`, `*Exception`, `*Mapper`.
Pacote raiz: `com.joao.adotec`.

### Sobre os bugs conhecidos

A seção **Bugs Conhecidos** em `references/arquitetura.md` lista problemas que ainda NÃO foram corrigidos intencionalmente. Não "corrija" esses comportamentos sem instrução explícita do desenvolvedor — isso pode introduzir regressões. Se uma tarefa tocar um bug listado, sinalize antes de prosseguir.

## Stack resumida

- Java 21 · Spring Boot 4.0.4 · Spring Data JPA · MySQL 8
- Spring Security + JWT stateless (`Authorization: Bearer <token>` — nunca cookie)
- MapStruct 1.6.3 · Lombok · springdoc-openapi 3.0.2
- Testes: JUnit 5 + Mockito + MockMvc

## Referências

- Arquitetura completa, regras de negócio e bugs: `references/arquitetura.md`
- Repositório: https://github.com/JoaoRobrt/AdoTEC
