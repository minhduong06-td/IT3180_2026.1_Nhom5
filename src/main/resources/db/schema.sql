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

CREATE TABLE IF NOT EXISTS fee_types (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    calculation_basis ENUM('PER_M2', 'PER_RESIDENT', 'FIXED', 'VOLUNTARY') NOT NULL,
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
    calculation_basis_snapshot ENUM('PER_M2', 'PER_RESIDENT', 'FIXED') NOT NULL,
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
    amount DECIMAL(15, 0) NOT NULL,
    method ENUM('CASH', 'BANK_TRANSFER') NOT NULL,
    reference VARCHAR(120) NULL,
    note VARCHAR(500) NULL,
    recorded_by BIGINT NOT NULL,
    KEY ix_payment_charge (charge_id),
    KEY ix_payment_paid_at (paid_at),
    CONSTRAINT chk_payment_positive CHECK (amount > 0),
    CONSTRAINT fk_payment_charge FOREIGN KEY (charge_id) REFERENCES charges(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_user FOREIGN KEY (recorded_by) REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS contributions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fee_type_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(15, 0) NOT NULL,
    method ENUM('CASH', 'BANK_TRANSFER') NOT NULL,
    reference VARCHAR(120) NULL,
    note VARCHAR(500) NULL,
    recorded_by BIGINT NOT NULL,
    KEY ix_contribution_received_at (received_at),
    KEY ix_contribution_apartment (apartment_id),
    CONSTRAINT chk_contribution_positive CHECK (amount > 0),
    CONSTRAINT fk_contribution_fee FOREIGN KEY (fee_type_id) REFERENCES fee_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contribution_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contribution_user FOREIGN KEY (recorded_by) REFERENCES app_users(id) ON DELETE RESTRICT
);
