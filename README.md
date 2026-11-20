# Wallet Ledger

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.2.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%2B-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![OpenAPI](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203-85EA2D?logo=swagger&logoColor=black)](https://springdoc.org/)

Backend quản lý **ví tài chính (Financial Wallet)** và **sổ cái kế toán kép (Double-Entry Ledger)** xây dựng bằng **Java 25**, **Spring Boot 4.2.0**, và **PostgreSQL** theo kiến trúc **Modular Monolith (Package-by-Feature)**.

---

## Mục lục

- [Tính năng nổi bật](#tính-năng-nổi-bật)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Kiến trúc & Cấu trúc thư mục](#kiến-trúc--cấu-trúc-thư-mục)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt & Chạy dự án](#cài-đặt--chạy-dự-án)
- [Biến môi trường](#biến-môi-trường)
- [API & Swagger UI](#api--swagger-ui)
- [Chuẩn phản hồi lỗi (RFC 7807) & Đa ngôn ngữ (i18n)](#chuẩn-phản-hồi-lỗi-rfc-7807--đa-ngôn-ngữ-i18n)
- [Kiểm thử (Testing)](#kiểm-thử-testing)
- [Lộ trình phát triển](#lộ-trình-phát-triển)

---

## Tính năng nổi bật

- **Xác thực & Quản lý phiên an toàn (`auth`):**
  - Đăng ký, đăng nhập, làm mới token (**Refresh Token Rotation** lưu dạng SHA-256 hash trong DB), đăng xuất thu hồi phiên, và xem thông tin tài khoản hiện tại (`/me`).
  - Mã hóa mật khẩu bằng **BCrypt (cost 12)**, xác thực stateless bằng **JWT (HS256)**.
  - Chống brute-force với cơ chế đếm số lần đăng nhập sai và tự động khóa tạm thời (`failed_login_attempts`, `locked_until`).
- **Chuẩn hóa hệ thống dùng chung (`common`):**
  - Xử lý lỗi tập trung theo chuẩn **RFC 7807 `ProblemDetail`** kèm `errorCode`, `messageKey`, `timestamp`.
  - Hỗ trợ đa ngôn ngữ **i18n** (Tiếng Anh mặc định & Tiếng Việt qua header `Accept-Language: vi`).
  - Phân trang an toàn (`PageQuery`, `PaginatedResponse`) có whitelist trường sắp xếp.
  - JPA Auditing (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`), Optimistic Locking (`@Version`), và Soft Delete.
- **Tài liệu API tương tác:**
  - Tích hợp sẵn **Swagger UI (OpenAPI 3)** hỗ trợ nhập **Bearer JWT** trực tiếp và tự động mở trình duyệt khi chạy local.

---

## Công nghệ sử dụng

| Thành phần | Công nghệ / Phiên bản |
| :--- | :--- |
| Ngôn ngữ | Java 25 |
| Framework | Spring Boot 4.2.0 (Web MVC, Data JPA, Security, OAuth2 Resource Server, Validation) |
| Cơ sở dữ liệu | PostgreSQL + HikariCP |
| Schema Migration | Flyway (`flyway-database-postgresql`) |
| Tài liệu API | Springdoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 3.1.1) |
| Build Tool | Maven Wrapper (`./mvnw` / `mvnw.cmd`) |
| Tiện ích | Lombok |

---

## Kiến trúc & Cấu trúc thư mục

Dự án áp dụng **Package-by-Feature** với ranh giới tầng rõ ràng (`api` → `application` → `domain` → `infrastructure`):

```text
wallet-ledger/
├── src/main/java/com/doht/wallet_ledger/
│   ├── WalletLedgerApplication.java
│   ├── auth/
│   │   ├── api/                 # Thin @RestController (AuthController)
│   │   ├── application/
│   │   │   ├── dto/             # Java record Request/Response DTOs
│   │   │   └── handler/         # Use-case handlers (@Transactional)
│   │   ├── domain/              # Enums (UserRole, UserStatus) & domain exceptions
│   │   └── infrastructure/      # JPA Entities, Repositories, JWT & SecurityConfig
│   └── common/
│       ├── exception/           # DomainException & GlobalExceptionHandler (RFC 7807)
│       ├── i18n/                # MessageKeys & MessageService
│       ├── openapi/             # OpenApiConfig & SwaggerBrowserLauncher
│       ├── pagination/          # PageQuery & PaginatedResponse
│       └── persistence/         # BaseEntity, BaseAuditableEntity, BaseSoftDeletableEntity
├── src/main/resources/
│   ├── application.yaml
│   ├── messages.properties      # i18n (English)
│   ├── messages_vi.properties   # i18n (Tiếng Việt)
│   └── db/migration/            # Flyway SQL migrations (V1__create_auth_tables.sql, ...)
└── src/test/java/               # Unit, @WebMvcTest slice & SpringBootTest
```

### Nguyên tắc thiết kế cốt lõi

- **Không rò rỉ `@Entity`** ra ngoài tầng persistence/application; mọi DTO đều là Java `record`.
- **`spring.jpa.open-in-view: false`** và `hibernate.ddl-auto: validate` (mọi thay đổi schema đi qua Flyway).
- Mọi timestamp lưu ở chuẩn **UTC** (`java.time.Instant` + `TIMESTAMPTZ`).

---

## Yêu cầu hệ thống

- **JDK 25** trở lên
- **PostgreSQL** (local hoặc cloud như Neon/Supabase)
- Không cần cài Maven sẵn (sử dụng `./mvnw` hoặc `mvnw.cmd`)

---

## Cài đặt & Chạy dự án

### 1. Clone repository

```bash
git clone https://github.com/HTD-0307/wallet-ledger.git
cd wallet-ledger
```

### 2. Tạo file cấu hình `.env`

Sao chép từ `.env.example` và điền thông tin database cùng khóa bí mật JWT của bạn:

```powershell
Copy-Item .env.example .env
```

*(macOS / Linux: `cp .env.example .env`)*

> **Lưu ý:** File `.env` đã nằm trong `.gitignore` — tuyệt đối không commit file `.env` lên Git.

### 3. Chạy ứng dụng

**Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

Khi khởi động xong:
- Flyway tự động kiểm tra và chạy migration trong `src/main/resources/db/migration/`.
- Server lắng nghe tại `http://localhost:8080`.
- Trình duyệt mặc định sẽ **tự động mở Swagger UI** tại `http://localhost:8080/swagger-ui.html`.

---

## Biến môi trường

| Biến | Bắt buộc | Mặc định | Mô tả |
| :--- | :---: | :--- | :--- |
| `DB_URL` | Có | — | JDBC URL kết nối PostgreSQL |
| `DB_USERNAME` | Có | — | Tên đăng nhập PostgreSQL |
| `DB_PASSWORD` | Có | — | Mật khẩu PostgreSQL |
| `JWT_SECRET` | Có | — | Chuỗi bí mật ký JWT HS256 (**tối thiểu 32 ký tự**) |
| `JWT_ISSUER` | Không | `wallet-ledger` | Giá trị claim `iss` trong JWT |
| `SWAGGER_ENABLED` | Không | `true` | Đặt `false` để tắt Swagger UI & `/v3/api-docs` trên production |
| `SWAGGER_AUTO_OPEN` | Không | `true` | Đặt `false` để tắt tự động mở trình duyệt khi chạy |

---

## API & Swagger UI

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Auth Endpoints (`/api/v1/auth`)

| Method | Endpoint | Xác thực | Mô tả |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản mới và trả về cặp token |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập bằng email & mật khẩu |
| `POST` | `/api/v1/auth/refresh` | Public | Đổi `refreshToken` lấy cặp token mới (rotation) |
| `GET` | `/api/v1/auth/me` | Bearer JWT | Lấy thông tin hồ sơ người dùng hiện tại |
| `POST` | `/api/v1/auth/logout` | Bearer JWT | Đăng xuất và thu hồi `refreshToken` |

### Hướng dẫn test nhanh trên Swagger UI

1. Mở `http://localhost:8080/swagger-ui.html`.
2. Gọi `POST /api/v1/auth/register` hoặc `POST /api/v1/auth/login` để nhận `accessToken`.
3. Nhấn nút **Authorize** ở góc trên bên phải → dán `accessToken` → **Authorize**.
4. Gọi thử `GET /api/v1/auth/me`.

---

## Chuẩn phản hồi lỗi (RFC 7807) & Đa ngôn ngữ (i18n)

Mọi lỗi đều được chuẩn hóa theo định dạng `application/problem+json`. Gửi header `Accept-Language: vi` để nhận thông báo tiếng Việt (mặc định là tiếng Anh):

```json
{
  "type": "urn:wallet-ledger:error:validation-failed",
  "title": "Dữ liệu không hợp lệ",
  "status": 400,
  "detail": "Một hoặc nhiều trường dữ liệu không hợp lệ.",
  "errorCode": "VALIDATION_FAILED",
  "messageKey": "common.error.validation.failed.detail",
  "timestamp": "2026-11-20T03:10:47.862011Z",
  "errors": {
    "email": ["Email không đúng định dạng."]
  }
}
```

---

## Kiểm thử (Testing)

Chạy toàn bộ bộ test (Unit tests, `@WebMvcTest` slice tests, và `WalletLedgerApplicationTests`):

```powershell
# Windows
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

---

## Lộ trình phát triển

- [x] Nền tảng dự án (Java 25, Spring Boot 4.2, PostgreSQL, Flyway, RFC 7807, i18n, Pagination)
- [x] Module `auth` (JWT HS256, Refresh Token Rotation, Brute-force lockout)
- [x] Tích hợp OpenAPI 3 / Swagger UI
- [ ] Module `wallet` (Quản lý ví đa tiền tệ, số dư chính xác với `BigDecimal`)
- [ ] Module `ledger` (Sổ cái kế toán kép bất biến — append-only double-entry)
- [ ] Module `transfer` (Nạp / Rút / Chuyển tiền với Idempotency-Key & Pessimistic Locking chống deadlock)
