# Docker Demo Database Implementation Plan

**Goal:** Add a repeatable, varied BlueMoon demo database that Docker loads before the desktop app starts.

**Architecture:** Reuse `src/main/resources/db/schema.sql` as the first MySQL init script and add `database/demo-data.sql` as the second. The Java bootstrap remains migration-only, while Compose owns clean-database demo initialization.

**Tech Stack:** MySQL 8.4, Docker Compose, Java 25, JUnit 5, Maven Wrapper.

**Spec:** `docs/superpowers/specs/2026-11-11-demo-database-design.md`

## Global Constraints

- Preserve the current schema and application data model.
- Apartment floor is derived from the apartment code; do not add a new floor column.
- A clean Docker volume must load schema before demo data.
- Demo data must be varied and valid under existing foreign keys/check constraints.

## Review Focus

- Init ordering: schema must exist before inserts.
- Idempotence: rerunning the SQL must not create duplicate demo records.
- Floor diversity: sample apartment codes must produce multiple floors.
- Relationship validity: each non-empty household has exactly one HEAD and valid resident dates.
- Application compatibility: login, apartment listing, billing and existing tests must still work.

### Task 1: Add seed contract test

**Files:**
- Create: `src/test/java/com/bluemoon/config/DemoDataSqlTest.java`
- Test: `database/demo-data.sql`

- [ ] Assert the SQL file exists, contains all expected apartment codes, contains varied floor codes, contains occupied/rented/vacant states, and contains more than one resident relationship.
- [ ] Run the focused test and verify it fails because the SQL file is absent.

### Task 2: Add varied demo data and Docker init wiring

**Files:**
- Create: `database/demo-data.sql`
- Modify: `compose.yaml`

- [ ] Add BCrypt accounts, apartment/resident records and representative operational data using existing schema names.
- [ ] Mount schema as `01-schema.sql` and demo data as `02-demo-data.sql`.
- [ ] Keep inserts idempotent with `INSERT ... ON DUPLICATE KEY UPDATE` or guarded `INSERT ... SELECT` statements.

### Task 3: Document startup and reset

**Files:**
- Modify: `README.md`

- [ ] Document clean-volume initialization, normal startup, app launch, and reset behavior.
- [ ] State that changing seed data requires recreating the volume.

### Task 4: Verify

- [ ] Run focused tests, full Maven tests, package build, and Docker Compose validation.
- [ ] On a clean volume, verify counts, floor distribution, statuses, resident diversity, and both demo logins.

