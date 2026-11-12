-- BlueMoon demo data for a clean Docker database.
-- This file is loaded after src/main/resources/db/schema.sql.
-- It is intentionally varied for classroom demonstration and local testing.

USE bluemoon;
SET NAMES utf8mb4;

-- Demo accounts: admin/admin123, accountant/accountant123 and resident/resident123.
-- The resident account is linked to apartment A101 after apartments are seeded below.
INSERT INTO app_users (username, password_hash, display_name, role, apartment_id)
VALUES
    ('admin', '$2a$10$yy6S51.swB9.1eFqHWOCpukuVPtcBoFJ3Ar3S5gZGeaACz2EekFbC', 'Ban quản trị BlueMoon', 'ADMIN', NULL),
    ('accountant', '$2a$10$zBuNJQo8bdNgn5A8qZApaexDXi9CxM01bY943w3/ZLy8YAVFZFN7G', 'Lê Thu Ngân', 'ACCOUNTANT', NULL),
    ('resident', '$2a$10$Vgtp/jmFwlRZsuxvkwO1PepcgQB8LsSa3Ss98bGa47OVEnjoEBpny', 'Nguyễn Minh An', 'RESIDENT', NULL)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    role = VALUES(role),
    apartment_id = VALUES(apartment_id),
    active = TRUE;

INSERT IGNORE INTO account_login_state (user_id)
SELECT id FROM app_users WHERE username IN ('admin', 'accountant', 'resident');

-- Apartments span floors 1, 2, 3, 4, 5, 6, 7, 8, 10 and 12.
-- The floor is intentionally derived from each apartment code by the application.
INSERT INTO apartments
    (code, household_code, area_m2, household_name, contact_phone, move_in_date, occupancy_status, active)
VALUES
    ('A101',  'HK-A101',  48.00,  'Nguyễn Minh An',  '0903123401', '2021-08-01', 'OCCUPIED', TRUE),
    ('A204',  'HK-A204',  52.50,  'Trần Gia Bảo',    '0912456702', '2022-02-15', 'OCCUPIED', TRUE),
    ('A305',  'HK-A305',  65.00,  'Lê Thu Hà',       '0988765403', '2022-05-20', 'OCCUPIED', TRUE),
    ('A407',  'HK-A407',  70.00,  'Phạm Quốc Huy',   '0935112204', '2023-01-10', 'RENTED',   TRUE),
    ('A508',  'HK-A508',  78.50,  'Đỗ Mai Anh',       '0977118805', '2021-11-03', 'OCCUPIED', TRUE),
    ('A612',  'HK-A612',  86.25,  'Vũ Hoàng Nam',     '0904987606', '2024-03-12', 'OCCUPIED', TRUE),
    ('B703',  'HK-B703',  92.00,  'Nguyễn Lan Chi',   '0966345707', '2024-06-01', 'OCCUPIED', TRUE),
    ('B809',  'HK-B809', 105.50,  'Bùi Đức Long',     '0918234508', '2022-09-18', 'RENTED',   TRUE),
    ('C1002', 'HK-C1002', 43.75,  'Hoàng Ngọc Linh',  '0888123409', '2025-01-12', 'OCCUPIED', TRUE),
    ('C1205', 'HK-C1205', 120.00, 'Căn hộ đang hoàn thiện', '0243765120', '2025-06-10', 'VACANT', FALSE)
ON DUPLICATE KEY UPDATE
    household_code = VALUES(household_code),
    area_m2 = VALUES(area_m2),
    household_name = VALUES(household_name),
    contact_phone = VALUES(contact_phone),
    move_in_date = VALUES(move_in_date),
    occupancy_status = VALUES(occupancy_status),
    active = VALUES(active);

UPDATE app_users u
JOIN apartments a ON a.code = 'A101'
SET u.apartment_id = a.id
WHERE u.username = 'resident' AND u.role = 'RESIDENT';

-- Households intentionally have different sizes and relationships.
INSERT INTO residents
    (apartment_id, full_name, date_of_birth, gender, national_id, relationship, move_in_date)
SELECT a.id, d.full_name, d.date_of_birth, d.gender, d.national_id, d.relationship, d.move_in_date
FROM apartments a
JOIN (
    SELECT 'A101' AS code, 'Nguyễn Minh An' AS full_name, '1985-03-12' AS date_of_birth, 'MALE' AS gender, '001085000101' AS national_id, 'HEAD' AS relationship, '2021-08-01' AS move_in_date
    UNION ALL SELECT 'A101', 'Phạm Ngọc Mai', '1987-09-24', 'FEMALE', '001087000102', 'WIFE', '2021-08-01'
    UNION ALL SELECT 'A101', 'Nguyễn Minh Khôi', '2015-06-18', 'MALE', NULL, 'CHILD', '2021-08-01'
    UNION ALL SELECT 'A204', 'Trần Gia Bảo', '1979-01-30', 'MALE', '001079000201', 'HEAD', '2022-02-15'
    UNION ALL SELECT 'A204', 'Lê Thanh Hương', '1982-11-07', 'FEMALE', '001082000202', 'WIFE', '2022-02-15'
    UNION ALL SELECT 'A204', 'Trần Minh Đức', '2007-05-19', 'MALE', '001107000203', 'CHILD', '2022-02-15'
    UNION ALL SELECT 'A305', 'Lê Thu Hà', '1990-04-22', 'FEMALE', '001090000301', 'HEAD', '2022-05-20'
    UNION ALL SELECT 'A305', 'Lê Minh Quân', '1960-12-03', 'MALE', '001060000302', 'PARENT', '2022-05-20'
    UNION ALL SELECT 'A305', 'Lê Hoàng Nam', '2017-02-14', 'MALE', NULL, 'CHILD', '2022-05-20'
    UNION ALL SELECT 'A305', 'Lê Ngọc Anh', '2019-09-26', 'FEMALE', NULL, 'CHILD', '2022-05-20'
    UNION ALL SELECT 'A407', 'Phạm Quốc Huy', '1988-07-11', 'MALE', '001088000401', 'HEAD', '2023-01-10'
    UNION ALL SELECT 'A407', 'Đặng Ngọc Linh', '1991-02-16', 'FEMALE', '001091000402', 'WIFE', '2023-01-10'
    UNION ALL SELECT 'A508', 'Đỗ Mai Anh', '1986-05-09', 'FEMALE', '001086000501', 'HEAD', '2021-11-03'
    UNION ALL SELECT 'A508', 'Đỗ Khánh Ngân', '2012-08-21', 'FEMALE', NULL, 'CHILD', '2021-11-03'
    UNION ALL SELECT 'A612', 'Vũ Hoàng Nam', '1983-10-14', 'MALE', '001083000601', 'HEAD', '2024-03-12'
    UNION ALL SELECT 'A612', 'Nguyễn Hải Yến', '1985-08-05', 'FEMALE', '001085000602', 'WIFE', '2024-03-12'
    UNION ALL SELECT 'A612', 'Vũ Minh Khang', '2010-11-29', 'MALE', '001110000603', 'CHILD', '2024-03-12'
    UNION ALL SELECT 'A612', 'Vũ Hà My', '2014-03-17', 'FEMALE', '001114000604', 'CHILD', '2024-03-12'
    UNION ALL SELECT 'A612', 'Vũ Văn Tâm', '1958-06-02', 'MALE', '001058000605', 'PARENT', '2024-03-12'
    UNION ALL SELECT 'B703', 'Nguyễn Lan Chi', '1994-06-27', 'FEMALE', '001094000701', 'HEAD', '2024-06-01'
    UNION ALL SELECT 'B809', 'Bùi Đức Long', '1976-09-19', 'MALE', '001076000801', 'HEAD', '2022-09-18'
    UNION ALL SELECT 'B809', 'Bùi Đức Anh', '2004-01-08', 'MALE', '001104000802', 'TENANT', '2024-09-01'
    UNION ALL SELECT 'C1002', 'Hoàng Ngọc Linh', '1992-12-15', 'FEMALE', '001092001001', 'HEAD', '2025-01-12'
    UNION ALL SELECT 'C1002', 'Ngô Thành Đạt', '1990-08-03', 'MALE', '001090001002', 'HUSBAND', '2025-01-12'
    UNION ALL SELECT 'C1002', 'Hoàng Gia Hân', '2021-04-30', 'FEMALE', NULL, 'CHILD', '2025-01-12'
) d ON d.code = a.code
WHERE a.active = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM residents existing
      WHERE existing.apartment_id = a.id
        AND existing.relationship = d.relationship
  );

-- Mandatory Sprint 1 fees plus inactive extension fees.
INSERT INTO fee_types (code, name, required, calculation_basis, active)
VALUES
    ('SERVICE',              'Phí dịch vụ',          TRUE,  'PER_M2',           TRUE),
    ('MANAGEMENT',           'Phí quản lý',          TRUE,  'PER_M2',           TRUE),
    ('SANITATION',           'Phí vệ sinh',          TRUE,  'PER_M2',           FALSE),
    ('MOTORCYCLE_PARKING',   'Phí gửi xe máy',       TRUE,  'PER_MOTORCYCLE',   FALSE),
    ('CAR_PARKING',          'Phí gửi ô tô',         TRUE,  'PER_CAR',          FALSE),
    ('UTILITY_ELECTRICITY',  'Tiền điện',            TRUE,  'EXTERNAL_BILL',    FALSE),
    ('UTILITY_WATER',        'Tiền nước',            TRUE,  'EXTERNAL_BILL',    FALSE),
    ('UTILITY_INTERNET',     'Tiền internet',        TRUE,  'EXTERNAL_BILL',    FALSE),
    ('COMMUNITY_FUND',       'Quỹ vì cộng đồng',     FALSE, 'VOLUNTARY',        TRUE)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    required = VALUES(required),
    calculation_basis = VALUES(calculation_basis),
    active = VALUES(active);

INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by)
SELECT f.id, x.unit_price, x.effective_from, u.id
FROM fee_types f
JOIN (
    SELECT 'SERVICE' AS code, 10000.00 AS unit_price, '2026-01-01' AS effective_from
    UNION ALL SELECT 'MANAGEMENT', 7000.00, '2026-01-01'
    UNION ALL SELECT 'SANITATION', 2500.00, '2026-01-01'
    UNION ALL SELECT 'MOTORCYCLE_PARKING', 70000.00, '2026-01-01'
    UNION ALL SELECT 'CAR_PARKING', 1200000.00, '2026-01-01'
) x ON x.code = f.code
JOIN app_users u ON u.username = 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM fee_rates r WHERE r.fee_type_id = f.id AND r.effective_from = x.effective_from
);

-- A current period with mandatory service and management charges only.
INSERT INTO billing_periods (period_year, period_month, issue_date, due_date, created_by)
SELECT 2026, 11, '2026-11-01', '2026-11-15', id
FROM app_users WHERE username = 'admin'
ON DUPLICATE KEY UPDATE issue_date = VALUES(issue_date), due_date = VALUES(due_date);

INSERT INTO charges
    (billing_period_id, apartment_id, fee_type_id, calculation_basis_snapshot,
     quantity_snapshot, unit_price_snapshot, amount)
SELECT p.id, a.id, f.id, 'PER_M2', a.area_m2, r.unit_price,
       ROUND(a.area_m2 * r.unit_price)
FROM billing_periods p
JOIN apartments a ON a.active = TRUE
JOIN fee_types f ON f.code IN ('SERVICE', 'MANAGEMENT') AND f.active = TRUE AND f.required = TRUE
JOIN fee_rates r ON r.fee_type_id = f.id AND r.effective_from = (
    SELECT MAX(r2.effective_from)
    FROM fee_rates r2
    WHERE r2.fee_type_id = f.id AND r2.effective_from <= p.issue_date
)
WHERE p.period_year = 2026 AND p.period_month = 11
  AND NOT EXISTS (
      SELECT 1 FROM charges c
      WHERE c.billing_period_id = p.id AND c.apartment_id = a.id AND c.fee_type_id = f.id
  );

-- Payments demonstrate unpaid, partial, paid and overpaid/credit states.
INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-03 09:10:00', 'Nguyễn Minh An', 100000, 100000, 'CASH',
       'BM-SEED-A101-SERVICE-1', 'Nộp một phần tại văn phòng', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A101' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A101-SERVICE-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-04 14:20:00', 'Phạm Ngọc Mai', c.amount, c.amount, 'BANK_TRANSFER',
       'BM-SEED-A101-MANAGEMENT-1', 'Đã đối soát sao kê', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A101' AND f.code = 'MANAGEMENT'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A101-MANAGEMENT-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-05 08:35:00', 'Trần Gia Bảo', c.amount, c.amount, 'BANK_TRANSFER',
       'BM-SEED-A204-SERVICE-1', 'Thanh toán đủ phí dịch vụ', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A204' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A204-SERVICE-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-06 16:45:00', 'Lê Thu Hà', c.amount, c.amount, 'CASH',
       'BM-SEED-A305-SERVICE-1', 'Thanh toán tại quầy', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A305' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A305-SERVICE-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-06 16:50:00', 'Lê Thu Hà', c.amount, c.amount, 'CASH',
       'BM-SEED-A305-MANAGEMENT-1', 'Thanh toán cùng đợt', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A305' AND f.code = 'MANAGEMENT'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A305-MANAGEMENT-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-07 10:05:00', 'Phạm Quốc Huy', 350000, 350000, 'CASH',
       'BM-SEED-A407-SERVICE-1', 'Nộp trước một phần', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'A407' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-A407-SERVICE-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-08 11:40:00', 'Nguyễn Lan Chi', c.amount, c.amount, 'BANK_TRANSFER',
       'BM-SEED-B703-SERVICE-1', 'Thanh toán tự nguyện qua ngân hàng', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'B703' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-B703-SERVICE-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-09 15:25:00', 'Bùi Đức Long', c.amount, c.amount, 'BANK_TRANSFER',
       'BM-SEED-B809-MANAGEMENT-1', 'Thanh toán phí quản lý', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'B809' AND f.code = 'MANAGEMENT'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-B809-MANAGEMENT-1');

INSERT INTO payments
    (charge_id, paid_at, payer_name, amount, applied_amount, method, reference, note, recorded_by)
SELECT c.id, '2026-11-10 09:15:00', 'Hoàng Ngọc Linh', 600000, c.amount, 'BANK_TRANSFER',
       'BM-SEED-C1002-CREDIT-1', 'Nộp dư, phần dư chuyển thành số dư hộ', u.id
FROM charges c JOIN apartments a ON a.id = c.apartment_id JOIN fee_types f ON f.id = c.fee_type_id
JOIN app_users u ON u.username = 'accountant'
WHERE a.code = 'C1002' AND f.code = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.reference = 'BM-SEED-C1002-CREDIT-1');

INSERT INTO apartment_credits (apartment_id, source_payment_id, amount, remaining_amount)
SELECT a.id, p.id, p.amount - p.applied_amount, p.amount - p.applied_amount
FROM payments p JOIN charges c ON c.id = p.charge_id JOIN apartments a ON a.id = c.apartment_id
WHERE p.reference = 'BM-SEED-C1002-CREDIT-1'
  AND NOT EXISTS (SELECT 1 FROM apartment_credits x WHERE x.source_payment_id = p.id);

-- Voluntary contributions are recorded separately from mandatory charges.
INSERT INTO contributions
    (fee_type_id, apartment_id, received_on, amount, method, reference, note, recorded_by)
SELECT f.id, a.id, x.received_on, x.amount, x.method, x.reference, x.note, u.id
FROM fee_types f
JOIN apartments a ON a.code = 'A101'
JOIN app_users u ON u.username = 'accountant'
JOIN (SELECT '2026-11-02' AS received_on, 50000 AS amount, 'CASH' AS method, 'BM-SEED-FUND-A101' AS reference, 'Ủng hộ quỹ cộng đồng' AS note) x
WHERE f.code = 'COMMUNITY_FUND'
  AND NOT EXISTS (SELECT 1 FROM contributions c WHERE c.reference = x.reference);

INSERT INTO contributions
    (fee_type_id, apartment_id, received_on, amount, method, reference, note, recorded_by)
SELECT f.id, a.id, '2026-11-03', 200000, 'BANK_TRANSFER', 'BM-SEED-FUND-A305', 'Đóng góp tự nguyện', u.id
FROM fee_types f JOIN apartments a ON a.code = 'A305' JOIN app_users u ON u.username = 'accountant'
WHERE f.code = 'COMMUNITY_FUND'
  AND NOT EXISTS (SELECT 1 FROM contributions c WHERE c.reference = 'BM-SEED-FUND-A305');

INSERT INTO contributions
    (fee_type_id, apartment_id, received_on, amount, method, reference, note, recorded_by)
SELECT f.id, a.id, '2026-11-05', 100000, 'CASH', 'BM-SEED-FUND-B703', 'Đóng góp cho hoạt động thiếu nhi', u.id
FROM fee_types f JOIN apartments a ON a.code = 'B703' JOIN app_users u ON u.username = 'accountant'
WHERE f.code = 'COMMUNITY_FUND'
  AND NOT EXISTS (SELECT 1 FROM contributions c WHERE c.reference = 'BM-SEED-FUND-B703');

-- Vehicles include both types and one historical inactive registration.
INSERT INTO vehicles (apartment_id, vehicle_type, license_plate, active, registered_by)
SELECT a.id, 'MOTORCYCLE', '29A1-123.45', TRUE, u.id FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A101' AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.license_plate = '29A1-123.45');
INSERT INTO vehicles (apartment_id, vehicle_type, license_plate, active, registered_by)
SELECT a.id, 'CAR', '30K-456.78', TRUE, u.id FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A305' AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.license_plate = '30K-456.78');
INSERT INTO vehicles (apartment_id, vehicle_type, license_plate, active, registered_by)
SELECT a.id, 'MOTORCYCLE', '36B2-789.01', TRUE, u.id FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A407' AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.license_plate = '36B2-789.01');
INSERT INTO vehicles (apartment_id, vehicle_type, license_plate, active, registered_by)
SELECT a.id, 'CAR', '51H-222.33', TRUE, u.id FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'B809' AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.license_plate = '51H-222.33');
INSERT INTO vehicles (apartment_id, vehicle_type, license_plate, active, registered_by)
SELECT a.id, 'MOTORCYCLE', '29C1-999.88', FALSE, u.id FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'C1002' AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.license_plate = '29C1-999.88');

-- Utility bills are inputs for later billing; they do not alter Sprint 1 mandatory charges.
INSERT INTO utility_bills
    (period_year, period_month, apartment_id, utility_type, amount, provider, reference, recorded_by)
SELECT 2026, 11, a.id, x.utility_type, x.amount, x.provider, x.reference, u.id
FROM apartments a JOIN app_users u ON u.username = 'accountant'
JOIN (SELECT 'A101' AS code, 'ELECTRICITY' AS utility_type, 356000 AS amount, 'EVN Hanoi' AS provider, 'EVN-2611-A101' AS reference
      UNION ALL SELECT 'A101', 'WATER', 182000, 'BlueMoon Water', 'WATER-2611-A101'
      UNION ALL SELECT 'A204', 'ELECTRICITY', 428000, 'EVN Hanoi', 'EVN-2611-A204'
      UNION ALL SELECT 'A204', 'INTERNET', 220000, 'VNPT', 'NET-2611-A204'
      UNION ALL SELECT 'A612', 'WATER', 315000, 'BlueMoon Water', 'WATER-2611-A612'
      UNION ALL SELECT 'B809', 'ELECTRICITY', 690000, 'EVN Hanoi', 'EVN-2611-B809'
      UNION ALL SELECT 'B809', 'INTERNET', 260000, 'FPT Telecom', 'NET-2611-B809'
      UNION ALL SELECT 'C1002', 'WATER', 146000, 'BlueMoon Water', 'WATER-2611-C1002') x ON x.code = a.code
WHERE NOT EXISTS (
    SELECT 1 FROM utility_bills b
    WHERE b.period_year = 2026 AND b.period_month = 11 AND b.apartment_id = a.id AND b.utility_type = x.utility_type
);

-- Residence changes show permanent, temporary residence and temporary absence.
INSERT INTO residence_changes (resident_id, status, start_date, end_date, reason, recorded_by)
SELECT r.id, 'PERMANENT_RESIDENCE', '2021-08-01', NULL, 'Đăng ký thường trú khi nhận căn hộ', u.id
FROM residents r JOIN apartments a ON a.id = r.apartment_id JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A101' AND r.relationship = 'HEAD'
  AND NOT EXISTS (SELECT 1 FROM residence_changes c WHERE c.resident_id = r.id AND c.start_date = '2021-08-01');
INSERT INTO residence_changes (resident_id, status, start_date, end_date, reason, recorded_by)
SELECT r.id, 'TEMPORARY_ABSENCE', '2026-11-08', '2026-11-20', 'Về quê giải quyết việc gia đình', u.id
FROM residents r JOIN apartments a ON a.id = r.apartment_id JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A204' AND r.relationship = 'WIFE'
  AND NOT EXISTS (SELECT 1 FROM residence_changes c WHERE c.resident_id = r.id AND c.start_date = '2026-11-08');
INSERT INTO residence_changes (resident_id, status, start_date, end_date, reason, recorded_by)
SELECT r.id, 'TEMPORARY_RESIDENCE', '2024-09-01', NULL, 'Đăng ký tạm trú theo hợp đồng thuê', u.id
FROM residents r JOIN apartments a ON a.id = r.apartment_id JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'B809' AND r.relationship = 'TENANT'
  AND NOT EXISTS (SELECT 1 FROM residence_changes c WHERE c.resident_id = r.id AND c.start_date = '2024-09-01');

-- Requests cover the main statuses and categories used by the UI.
INSERT INTO resident_requests
    (apartment_id, reporter_name, category, subject, description, status, handling_note, created_by)
SELECT a.id, 'Nguyễn Minh An', 'MAINTENANCE', 'Rò nước tại khu bếp', 'Nước rỉ nhẹ dưới chậu rửa từ tối qua.', 'RECEIVED', NULL, u.id
FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A101'
  AND NOT EXISTS (SELECT 1 FROM resident_requests r WHERE r.apartment_id = a.id AND r.category = 'MAINTENANCE');
INSERT INTO resident_requests
    (apartment_id, reporter_name, category, subject, description, status, handling_note, created_by)
SELECT a.id, 'Lê Thu Hà', 'SECURITY', 'Thẻ thang máy không hoạt động', 'Thẻ của hộ không mở được tầng 3.', 'IN_PROGRESS', 'Đã chuyển bộ phận kỹ thuật kiểm tra đầu đọc.', u.id
FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'A305'
  AND NOT EXISTS (SELECT 1 FROM resident_requests r WHERE r.apartment_id = a.id AND r.category = 'SECURITY');
INSERT INTO resident_requests
    (apartment_id, reporter_name, category, subject, description, status, handling_note, created_by)
SELECT a.id, 'Bùi Đức Long', 'CLEANING', 'Đã vệ sinh khu vực hành lang', 'Đề nghị vệ sinh lại hành lang trước cửa căn hộ.', 'RESOLVED', 'Đã xử lý ngày 09/11/2026.', u.id
FROM apartments a JOIN app_users u ON u.username = 'admin'
WHERE a.code = 'B809'
  AND NOT EXISTS (SELECT 1 FROM resident_requests r WHERE r.apartment_id = a.id AND r.category = 'CLEANING');
