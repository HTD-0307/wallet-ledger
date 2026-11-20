# Wallet Ledger — Principal Java Engineering Rules & Agent Architecture

This repository (`com.doht:wallet-ledger`) is a financial wallet & double-entry ledger backend built with **Java 25**, **Spring Boot 4.2.0**, **Spring Web MVC**, **Spring Data JPA**, **Spring Security**, **Bean Validation**, **Lombok**, and **PostgreSQL** (built with Maven `./mvnw`).

---

## 1. Architectural Principles

- **Package-by-Feature (Modular Monolith):** Organize under `com.doht.wallet_ledger.<feature>` (e.g., `account`, `wallet`, `ledger`, `transfer`, `auth`, `common`) instead of flat global layers. Keep package-private visibility where possible; expose feature capabilities through explicit service/API interfaces.
- **Strict Layer Boundaries:**
  - `api` / `web`: Thin `@RestController`s routing HTTP requests with `@Valid` to application handlers. **Never** put business logic or `@Transactional` here.
  - `application`: Request/Response Java `record` DTOs (`application/dto`) and use-case handlers (`application/handler`) owning transaction boundaries (`@Transactional`), locking, and idempotency checks.
  - `domain`: Core financial invariants, value objects (`Money`, `CurrencyCode`), domain exceptions, state transitions.
  - `infrastructure` / `persistence`: JPA `@Entity` classes, Spring Data repositories, database migrations. **Never** leak `@Entity` outside the persistence/application boundary.

---

## 2. Financial & Ledger Non-Negotiables

1. **Exact Money Arithmetic:** Always use `BigDecimal` (with explicit `scale` and `RoundingMode.HALF_EVEN`/`HALF_UP` as defined by business rules) or `long` minor units + ISO-4217 currency code. **Never** use `float` or `double`.
2. **Double-Entry Bookkeeping:** Every balance-affecting operation must produce balanced ledger entries (`SUM(debits) == SUM(credits)` per transaction and currency).
3. **Immutable Ledger:** Posted ledger entries are **append-only**. Never `UPDATE` or `DELETE` posted ledger rows; handle refunds/corrections via compensating/reversing entries.
4. **Idempotency:** Every mutating money endpoint (deposit, withdraw, transfer, hold, capture) must require and enforce an `Idempotency-Key` backed by a DB unique constraint.
5. **Concurrency & Deadlock Prevention:**
   - Guard balance mutations against lost updates and negative balances using pessimistic locking (`PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`) or optimistic locking (`@Version`) as designed for the operation.
   - When locking multiple wallets/accounts (e.g., transfer `A -> B`), **always acquire locks in deterministic ascending ID order** to prevent deadlocks.
   - Keep `@Transactional` scopes short; **never** perform remote HTTP calls inside a database transaction.

---

## 3. Java 25 & Spring Boot 4 Coding Standards

- **Modern Java 25:**
  - Write all code comments and Javadoc in **Vietnamese**.
  - Use `record` for DTOs, commands, queries, and immutable value objects.
  - Use `sealed interface` + exhaustive pattern matching `switch` for domain states, results, and typed errors.
  - Use `java.time.Instant` (UTC) for all persisted timestamps; never `java.util.Date` or `Calendar`.
  - Use `Optional` only as method return types, never as fields or method parameters.
- **Dependency Injection & Lombok:**
  - Constructor injection only (`@RequiredArgsConstructor` + `private final` fields). Never field `@Autowired`.
  - Use `@Slf4j` for structured logging. **Never** log secrets, tokens, passwords, or sensitive PII.
  - **Never** put `@Data` or default `@EqualsAndHashCode` / `@ToString` on JPA `@Entity` classes (causes lazy-loading explosions and broken `Set` contracts).
- **JPA & PostgreSQL Discipline:**
  - Set `spring.jpa.open-in-view: false`.
  - Always use `FetchType.LAZY` on `@ManyToOne` and `@OneToOne`. Avoid `EAGER` fetching.
  - Manage schema via versioned migrations (Flyway/Liquibase); never rely on `ddl-auto: update` in production.
  - Use `readOnly = true` on query-only `@Transactional` methods.
- **Error Handling & Security:**
  - Return standard **RFC 7807 `ProblemDetail`** responses via a global `@RestControllerAdvice`.
  - Enforce ownership authorization on every wallet/ledger query and mutation (prevent IDOR/BOLA).

---

## 4. Specialized Agent Suite (Routing Guide)

Activate the matching skill in `.agents/skills/` (or delegate to a subagent with that role) based on the task:

| Agent / Skill | Responsibility | Activate When |
| :--- | :--- | :--- |
| `java-ledger-architect` | Domain modeling, DB schema, double-entry invariants, locking & API contracts | Designing a new feature, schema, or architecture decision |
| `java-spring-implementer` | Idiomatic Java 25 / Spring Boot 4 implementation & migrations | Writing production code, endpoints, services, repositories |
| `java-security-auditor` | Spring Security config, authN/authZ, IDOR/replay/financial threat audit | Touching security, auth, or money-moving endpoints |
| `java-qa-test-engineer` | Unit, Spring Boot 4 slice, Testcontainers PostgreSQL & concurrency tests | Writing/fixing tests, proving invariants & thread safety |
| `java-code-reviewer` | Staff-level review for correctness, N+1 queries, locks, & clean code | Reviewing changes before completion or PR |

### Recommended Workflow for New Features
1. **Design** (`java-ledger-architect`) → agree on schema, invariants, lock order, and API contract.
2. **Implement** (`java-spring-implementer`) → write migration, domain, persistence, service, and web layers.
3. **Secure** (`java-security-auditor`) → verify authZ, validation, idempotency, and audit logging.
4. **Verify** (`java-qa-test-engineer`) → write unit, slice, integration, and concurrency tests; run `./mvnw test`.
5. **Review** (`java-code-reviewer`) → final Staff-level audit before reporting done.
