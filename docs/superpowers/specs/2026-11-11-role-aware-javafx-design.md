# Role-aware JavaFX demo design

## Intent

Apply the recent BlueMoon UI and demo-data changes to the JavaFX/MySQL repository,

## Goals

- Keep `ADMIN` as Ban quản trị/PM and `ACCOUNTANT` as Kế toán.
- Add `RESIDENT` as the normal user/cư dân role; do not add an Authority/coquan demo account.
- Link a resident account to one apartment and scope resident-facing reads to that apartment.
- Keep resident accounts read-only for charges/history and allow only resident-safe request/contribution actions.
- Keep admin-only account, apartment, fee, billing, vehicle, utility and residence-management actions hidden and blocked.
- Keep the concise existing visual language and remove redundant role/technical copy.
- Seed varied demo data without changing the existing Sprint 1 billing flow.

## Non-goals

- No new payment gateway, notification channel, or Sprint 2 module.
- No replacement of the JavaFX application with React.
- No `AUTHORITY`/`coquan` account.

## Implementation approach

Use the existing enum-based role model and repository/service layers. Add a nullable
`apartment_id` to `app_users`, load it into `AppUser`, and use it in read queries for
resident accounts. Keep backwards-compatible constructors and repository overloads so
existing unit and MySQL integration tests remain valid. Add tests for role labels,
demo-account contents, and resident authorization before production changes.

## Role visibility

- `ADMIN`: all current management screens.
- `ACCOUNTANT`: overview, residents, voluntary contributions, debts, charges and payment history.
- `RESIDENT`: overview, own residence, own charges/debt/payment history and resident requests/contributions.

The UI only presents permitted screens; service/repository checks remain authoritative.
