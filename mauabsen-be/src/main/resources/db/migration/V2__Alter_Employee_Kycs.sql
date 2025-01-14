-- Drop existing table if exists
DROP TABLE IF EXISTS employee_kycs CASCADE;

-- Create table with new structure
CREATE TABLE employee_kycs (
    id BIGSERIAL PRIMARY KEY,
    employee_id INTEGER,
    address VARCHAR(1000),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    fingerprint_id TEXT,
    photo_url VARCHAR(1000),
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    physical_fingerprint TEXT,
    device_fingerprint TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
); 