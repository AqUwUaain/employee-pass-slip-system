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
