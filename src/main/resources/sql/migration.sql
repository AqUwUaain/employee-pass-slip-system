-- Migration: Add new columns for requirements compliance
-- Run this against the PostgreSQL database

-- Add manager, email, address, emergency_contact to employees table
ALTER TABLE employees ADD COLUMN IF NOT EXISTS manager VARCHAR(255) DEFAULT '';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS email VARCHAR(255) DEFAULT '';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address TEXT DEFAULT '';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(255) DEFAULT '';

-- Add employee_id to activity_logs for structured filtering
ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS employee_id INT DEFAULT 0;

-- Add duration_minutes to pass_slips for numeric aggregation
ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS duration_minutes BIGINT DEFAULT 0;

-- Signatures table (1 signature per admin)
CREATE TABLE IF NOT EXISTS signatures (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    signature_name VARCHAR(255) NOT NULL,
    image_data BYTEA NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_signatures_user_id ON signatures (user_id);

-- Estimated return time for pass slips
ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS estimated_return TIMESTAMP DEFAULT NULL;

-- Actual time out (when employee really departed, may differ from slip creation time)
ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS actual_time_out TIMESTAMP DEFAULT NULL;

-- Staff/requester signature stored per pass slip
ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS requester_signature BYTEA DEFAULT NULL;

-- Signature requests table for staff approval workflow
CREATE TABLE IF NOT EXISTS signature_requests (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    signature_name VARCHAR(255) NOT NULL,
    image_data BYTEA NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    reviewed_by INT DEFAULT NULL,
    reviewed_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE signature_requests ADD CONSTRAINT fk_sig_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
