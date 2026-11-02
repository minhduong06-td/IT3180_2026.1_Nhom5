CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role ENUM('ADMIN', 'ACCOUNTANT') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_login_state (
    user_id BIGINT NOT NULL PRIMARY KEY,
    failed_attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    CONSTRAINT fk_login_state_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS apartments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    household_code VARCHAR(20) NOT NULL,
    area_m2 DECIMAL(10, 2) NOT NULL,
    household_name VARCHAR(120) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    move_in_date DATE NOT NULL,
    occupancy_status ENUM('OCCUPIED', 'VACANT', 'RENTED') NOT NULL DEFAULT 'OCCUPIED',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_apartment_household_code (household_code),
    CONSTRAINT chk_apartment_area CHECK (area_m2 > 0)
);

CREATE TABLE IF NOT EXISTS residents (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    apartment_id BIGINT NOT NULL,
    full_name VARCHAR(50) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    national_id VARCHAR(12) NULL UNIQUE,
    relationship ENUM('HEAD', 'WIFE', 'HUSBAND', 'CHILD', 'PARENT', 'TENANT', 'OTHER') NOT NULL,
    head_apartment_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN relationship = 'HEAD' THEN apartment_id ELSE NULL END
    ) STORED,
    move_in_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_resident_household_head (head_apartment_id),
    KEY ix_resident_apartment (apartment_id),
    CONSTRAINT chk_resident_national_id CHECK (national_id IS NULL OR national_id REGEXP '^([0-9]{9}|[0-9]{12})$'),
    CONSTRAINT fk_resident_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    apartment_id BIGINT NOT NULL,
    vehicle_type ENUM('MOTORCYCLE', 'CAR') NOT NULL,
    license_plate VARCHAR(16) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    active_license_plate VARCHAR(16) GENERATED ALWAYS AS (IF(active, license_plate, NULL)) STORED,
    registered_by BIGINT NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_vehicle_active_plate (active_license_plate),
    KEY ix_vehicle_apartment_active_type (apartment_id, active, vehicle_type),
    CONSTRAINT fk_vehicle_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_vehicle_user FOREIGN KEY (registered_by) REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS fee_types (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    calculation_basis ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED', 'VOLUNTARY') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fee_rates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fee_type_id BIGINT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    effective_from DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_fee_rate_start (fee_type_id, effective_from),
    CONSTRAINT chk_fee_rate_positive CHECK (unit_price > 0),
    CONSTRAINT fk_fee_rate_type FOREIGN KEY (fee_type_id) REFERENCES fee_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fee_rate_user FOREIGN KEY (created_by) REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS billing_periods (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    period_year SMALLINT NOT NULL,
    period_month TINYINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_billing_month (period_year, period_month),
    CONSTRAINT chk_billing_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_billing_dates CHECK (due_date >= issue_date),
    CONSTRAINT fk_billing_user FOREIGN KEY (created_by) REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS charges (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    billing_period_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    fee_type_id BIGINT NOT NULL,
    calculation_basis_snapshot ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED') NOT NULL,
    quantity_snapshot DECIMAL(10, 2) NOT NULL,
    unit_price_snapshot DECIMAL(15, 2) NOT NULL,
    amount DECIMAL(15, 0) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_charge_apartment_fee_period (apartment_id, fee_type_id, billing_period_id),
    KEY ix_charge_period (billing_period_id),
    CONSTRAINT chk_charge_amount CHECK (amount >= 0),
    CONSTRAINT fk_charge_period FOREIGN KEY (billing_period_id) REFERENCES billing_periods(id) ON DELETE RESTRICT,
    CONSTRAINT fk_charge_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_charge_fee FOREIGN KEY (fee_type_id) REFERENCES fee_types(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    charge_id BIGINT NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payer_name VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 0) NOT NULL,
    applied_amount DECIMAL(15, 0) NOT NULL,
    method ENUM('CASH', 'BANK_TRANSFER') NOT NULL,
    reference VARCHAR(120) NULL,
    note VARCHAR(500) NULL,
    recorded_by BIGINT NOT NULL,
    KEY ix_payment_charge (charge_id),
    KEY ix_payment_paid_at (paid_at),
    CONSTRAINT chk_payment_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_applied CHECK (applied_amount >= 0 AND applied_amount <= amount),
    CONSTRAINT fk_payment_charge FOREIGN KEY (charge_id) REFERENCES charges(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_user FOREIGN KEY (recorded_by) REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS apartment_credits (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    apartment_id BIGINT NOT NULL,
    source_payment_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(15, 0) NOT NULL,
    remaining_amount DECIMAL(15, 0) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY ix_credit_apartment_balance (apartment_id, remaining_amount),
    CONSTRAINT chk_credit_amount CHECK (amount > 0 AND remaining_amount >= 0 AND remaining_amount <= amount),
    CONSTRAINT fk_credit_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_credit_payment FOREIGN KEY (source_payment_id) REFERENCES payments(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS credit_applications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    credit_id BIGINT NOT NULL,
    charge_id BIGINT NOT NULL,
    amount DECIMAL(15, 0) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_credit_charge (credit_id, charge_id),
    CONSTRAINT chk_credit_application_positive CHECK (amount > 0),
    CONSTRAINT fk_credit_application_credit FOREIGN KEY (credit_id) REFERENCES apartment_credits(id) ON DELETE RESTRICT,
    CONSTRAINT fk_credit_application_charge FOREIGN KEY (charge_id) REFERENCES charges(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS contributions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fee_type_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    received_on DATE NOT NULL,
    amount DECIMAL(15, 0) NOT NULL,
    method ENUM('CASH', 'BANK_TRANSFER') NOT NULL,
    reference VARCHAR(120) NULL,
    note VARCHAR(500) NULL,
    recorded_by BIGINT NOT NULL,
    KEY ix_contribution_received_on (received_on),
    KEY ix_contribution_apartment (apartment_id),
    CONSTRAINT chk_contribution_positive CHECK (amount > 0),
    CONSTRAINT fk_contribution_fee FOREIGN KEY (fee_type_id) REFERENCES fee_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contribution_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contribution_user FOREIGN KEY (recorded_by) REFERENCES app_users(id) ON DELETE RESTRICT
);
