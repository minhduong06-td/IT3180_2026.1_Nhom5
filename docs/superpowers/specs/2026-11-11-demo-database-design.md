# Demo Database Design

## Intent

Provide a repeatable Docker database for BlueMoon demonstrations with varied apartment floors, household sizes, resident relationships, occupancy states, contacts, vehicles, utilities, contributions, and requests.

## Scope

- Keep the existing application schema as the single schema source.
- Add one idempotent demo-data SQL script that runs after the schema.
- Mount both scripts into MySQL's initialization directory in Docker Compose.
- Keep Java bootstrap responsible for schema compatibility/migrations, not demo-data insertion.
- Document clean reset and startup commands.

## Data decisions

- Use apartment codes whose numeric portions represent floors already supported by `Domain.Apartment.floor()`.
- Include occupied, rented, and vacant apartments; the vacant sample is inactive and has no residents.
- Use 10 apartments across floors 1, 2, 3, 4, 5, 6, 7, 8, 10, and 12.
- Seed two login accounts with BCrypt hashes and representative financial/operational records.
- Keep Sprint 1 mandatory fees active; keep extension fee types available but inactive.

## Non-goals

- Do not add a floor column or change billing rules.
- Do not make Java insert the same demo records on every launch.
