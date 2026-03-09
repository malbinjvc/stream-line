# StreamLine - Error Log

## Build Errors

### Error #1: Gradle Plugin Resolution Failure
**Error:** `Plugin [id: 'io.spring.dependency-management', version: '1.1.10'] was not found in any of the following sources: Gradle Central Plugin Repository`
**Cause:** Version 1.1.10 of `io.spring.dependency-management` does not exist on Maven Central or Gradle Plugin Portal.
**Fix:** Changed version to `1.1.7` in `app/build.gradle.kts`. Also added `pluginManagement` block to `settings.gradle.kts` with `gradlePluginPortal()` and `mavenCentral()` repositories.

### Error #2: Gradle Plugin Repository Missing
**Error:** Even with correct version, plugin not found when only Gradle Central Plugin Repository was configured.
**Cause:** `settings.gradle.kts` lacked a `pluginManagement` block with proper plugin repositories.
**Fix:** Added `pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }` to `settings.gradle.kts`.

### Error #3: LazyInitializationException (3 test failures)
**Error:** `org.hibernate.LazyInitializationException` in `WorkflowControllerTest` and `EventControllerTest`
**Cause:** `Workflow.steps` was using default `FetchType.LAZY` on `@OneToMany`, and `open-in-view: false` in `application.yml`. When `toResponse()` called `steps.size` outside the Hibernate session, it threw.
**Fix:** Changed `@OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true)` to include `fetch = FetchType.EAGER` since step count is always needed in responses.

### Error #4: ConstraintViolationException (1 test failure)
**Error:** `org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException` when submitting an event
**Cause:** `Event.completedAt` was annotated with `@Column(nullable = false)` but initialized to `null`. Pending events don't have a completion time yet.
**Fix:** Removed `nullable = false` from the `completedAt` column annotation.

---

## Security Audit (10-Point Checklist)

| # | Category | Result | Notes |
|---|----------|--------|-------|
| 1 | Hardcoded Secrets | PASS | All credentials externalized via env vars |
| 2 | SQL Injection | PASS | All queries use parameterized JPQL via Spring Data JPA |
| 3 | Input Validation | PASS | @Valid + @NotBlank + @Size on request DTOs, UUID type-safety |
| 4 | Dependency Vulnerabilities | PASS | Spring Boot 3.5.1, all deps current. H2 scope fixed to runtimeOnly |
| 5 | Auth / Access Control | NOTE | API endpoints use permitAll() — acceptable for demo/portfolio project. Production would need JWT/OAuth2 |
| 6 | CSRF / XSS / Security Headers | PASS | CSRF disabled (stateless API), XSS protection, X-Frame-Options: DENY, CSP, HSTS, Permissions-Policy headers added |
| 7 | Sensitive Data Exposure | PASS | No stack traces returned, actuator health details hidden, H2 console disabled |
| 8 | Docker Security | PASS | Multi-stage build, alpine base, non-root user, healthcheck |
| 9 | CI Security | PASS | Pinned action versions, no secrets in workflow, tests in CI |
| 10 | Encryption at Rest/Transit | PASS | TLS handled by reverse proxy (standard container pattern), no sensitive data stored |

### Security Fixes Applied
1. Changed H2 dependency scope from `implementation` to `runtimeOnly`
2. Removed unused bcrypt dependency (no auth implementation to use it)
3. Added HSTS header with `includeSubDomains` and 1-year max-age
4. Added Content-Security-Policy: `default-src 'none'; frame-ancestors 'none'`
5. Added Permissions-Policy: `geolocation=(), camera=(), microphone=()`
