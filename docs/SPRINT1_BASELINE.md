# Sprint 1 baseline: BlueMoon

## Goal

Deliver a usable desktop flow for monthly mandatory-fee collection, from apartment setup through balance lookup and payment history.

## Included scope

- Administrator and accountant sign-in with role-based access.
- Apartment records with code, area, household contact, and phone.
- Required fee types with effective-dated rates.
- Monthly billing periods and area-based charge generation.
- A statement per apartment, with the fee, calculation, amount, and due date.
- Partial and full cash or manual bank-transfer payments.
- Current collection totals, outstanding debt, and searchable transaction history.
- Fixed demo data and a clean-database demo path.

## Adopted business rules

1. The two demo fees are service fee and management fee. Both use apartment area in m² multiplied by the effective unit price.
2. An issued charge snapshots area, unit price, formula, and amount. Later edits do not recalculate it.
3. A period can issue at most one charge for each apartment and fee type.
4. Payments are positive and cannot exceed the charge balance. Partial payments are allowed.
5. Payment records are append-only in Sprint 1. Cash or manual bank transfer is supported.
6. Required fees are generated for active apartments. Voluntary contributions are outside this sprint.
7. Administrators manage apartments, rates, and billing periods. Accountants review charges and record payments.

## Definition of Done

- Start from an empty database and create or load three apartments with different areas.
- Create the two required fees and effective-dated rates.
- Issue period 10/2026 and verify each apartment's amounts.
- Confirm a second issue attempt cannot duplicate charges.
- Open an apartment statement and see each formula, total, and due date.
- Record a partial and a full payment and verify balances and states.
- Confirm an unauthorized role cannot access restricted data or actions.
- Keep automated rule checks and a repeatable manual demo checklist in the repository.

## Planned schedule

| Dates | Work |
|---|---|
| 19–22 Oct 2026 | Confirm Sprint 1 scope, core ERD, fee formula, roles, Definition of Done, and demo data |
| 23–29 Oct 2026 | Implement the vertical fee-collection flow |
| 30 Oct 2026 | Test with a clean database and fix defects |
| 31 Oct 2026 | Internal demo and evidence preparation |

These are planning dates. Git stores actual commit timestamps; commits in this repository are not backdated.

## Excluded scope

Resident accounts, temporary residence, parking, utilities, automatic bank matching, QR payment, email/SMS delivery, resident portal, PDF or spreadsheet export, and full income/expense reporting.
