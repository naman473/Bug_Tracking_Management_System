# BugTracker — Spring Boot

A role-based bug tracking system for **Project Managers**, **Developers**, and **Testers**.

---

## Quick Start

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.9+

### Database Setup
```sql
CREATE DATABASE bugtrackerdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Run the SQL scripts in order:
```
sql/bugtrackerdb.sql      -- schema + seed data
sql/migration_v2.sql      -- bug_comment table
sql/migration_v3.sql      -- bug_activity audit table  ← NEW in v10
```

### Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### Run
```bash
mvn spring-boot:run
```
Open `http://localhost:8080`

---

## Default Test Credentials
All accounts use password: `Password1!`

| Email              | Role           |
|--------------------|----------------|
| user5@gmail.com    | ProjectManager |
| user8@gmail.com    | ProjectManager |
| user4@gmail.com    | Developer      |
| user6@gmail.com    | Tester         |

---

## Features

### Core
- **Three-role system** — Manager / Developer / Tester with Spring Security
- **Project creation** — Managers create projects and assign teams
- **Bug lifecycle** — Open → Assigned → Closed (manager must approve close)
- **Severity levels** — Critical / Major / Minor / Trivial
- **Comment threads** — All roles can comment on bugs
- **Bug filtering** — Filter by status and severity (manager view)
- **CSV import** — Bulk-import users from JSON allowlist file

### v10 Additions
- **Bug Activity Audit Trail** — Every REPORTED / ASSIGNED / MARKED / CLOSED / EDITED event recorded
- **Edit Bug** — Testers can update title, description, severity on open bugs
- **Developer Dashboard Stats** — Assigned bug count, open/critical counters
- **Manager Dashboard** — Added "Assigned" stat card, last login now properly updated
- **Tester Dashboard** — Fixed SpEL enum comparison crash
- **Password strength meter** — Visual strength bar on register + change-password pages
- **Client-side confirm-password** — Mis-match caught before form submit
- **Password eye toggle** — Show/hide password on login, register, change-password
- **Session-expired banner** — Login page shows a warning when session times out
- **Global error handler** — Friendly 404/500 pages replacing Spring Whitelabel
- **Logout fixed** — Changed from `<a href="/logout">` to POST form (GET logout disallowed by default in Spring Security 6)
- **Null-safe severity** — Templates no longer crash on bugs with null severity
- **Developer assigned-bugs filter** — Closed bugs hidden from developer's bug view
- **Mark-for-close guard** — Already-closed bugs cannot be re-flagged
- **Import Users page** — Added JSON format documentation
- **Report Bug page** — Added title character counter + no-project warning
- **HikariCP pool config** — Pool size made explicit in application.properties
- **Logging config** — Package-level log levels configured

---

## Architecture

```
controller/   HTTP layer — thin, delegates to services
service/      Business logic, activity logging, transactions
repository/   JDBC queries, row mappers
model/        Domain objects (records + enums)
config/       SecurityConfig, AppConfig, ProjectOwnershipInterceptor
util/         Routes, FilterUtils, PasswordValidator
```

### Key Design Decisions
- **No ORM** — Plain JDBC (JdbcTemplate) keeps SQL visible and N+1 avoidable
- **Sealed interfaces** — `RegistrationResult`, `ChangePasswordResult` for exhaustive handling
- **Enum enums** — `BugStatus`, `Severity`, `Role`, `ProjectState` replace raw strings everywhere
- **Command objects** — `CreateBugCommand`, `UpdateBugCommand` for immutable write payloads
- **AppUserDetails** — Carries userName + Role in session; avoids per-request DB lookups
- **BugStats record** — Typed pivot instead of `Map<String, Long>` with stringly-typed keys
- **ProjectOwnershipInterceptor** — Single EXISTS query on every manager project request
