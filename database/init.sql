-- ShopShield Database Initialization Script
-- This script creates the database structure with sample data

-- Create the database and schemas (this runs first via docker-compose)
\c shopshield;

-- Create schemas for different services
CREATE SCHEMA IF NOT EXISTS backend;
CREATE SCHEMA IF NOT EXISTS ocr_service;
CREATE SCHEMA IF NOT EXISTS cv_service;
CREATE SCHEMA IF NOT EXISTS compliance;

-- Set search path to include all schemas
SET search_path TO backend, ocr_service, cv_service, compliance, public;

-- Table for roles in the backend schema
CREATE TABLE IF NOT EXISTS backend.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

-- Insert default roles
INSERT INTO backend.roles (name) VALUES 
    ('ROLE_USER'), 
    ('ROLE_ADMIN') 
ON CONFLICT (name) DO NOTHING;

-- Table for application users
CREATE TABLE IF NOT EXISTS backend.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Table for user roles mapping
CREATE TABLE IF NOT EXISTS backend.user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY(user_id)
        REFERENCES backend.users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY(role_id)
        REFERENCES backend.roles(id)
        ON DELETE CASCADE
);

-- Table for OCR scan results
CREATE TABLE IF NOT EXISTS ocr_service.ocr_scan_results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    image_path VARCHAR(2048) NOT NULL,
    extracted_text TEXT,
    scan_metadata JSONB,
    compliance_result VARCHAR(255),
    scanned_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ocr_user
        FOREIGN KEY(user_id)
        REFERENCES backend.users(id)
        ON DELETE SET NULL
);

-- Table for Computer Vision scan results
CREATE TABLE IF NOT EXISTS cv_service.cv_scan_results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    image_path VARCHAR(2048) NOT NULL,
    detection_result VARCHAR(255),
    confidence_score NUMERIC(5, 4),
    analyzed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cv_user
        FOREIGN KEY(user_id)
        REFERENCES backend.users(id)
        ON DELETE SET NULL
);

-- Table for products in the compliance schema
CREATE TABLE IF NOT EXISTS compliance.products (
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(2048),
    weight NUMERIC(10, 2),
    price NUMERIC(10, 2),
    packaging_info TEXT,
    last_checked_timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Table for compliance violations
CREATE TABLE IF NOT EXISTS compliance.violations (
    violation_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    violated_rule_code VARCHAR(50),
    violated_rule_description TEXT,
    detected_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'unresolved' CHECK (status IN ('resolved', 'unresolved')),
    resolution_timestamp TIMESTAMPTZ,
    CONSTRAINT fk_product
        FOREIGN KEY(product_id)
        REFERENCES compliance.products(product_id)
        ON DELETE CASCADE
);

-- Table for violation resolutions
CREATE TABLE IF NOT EXISTS compliance.resolutions (
    resolution_id BIGSERIAL PRIMARY KEY,
    violation_id BIGINT UNIQUE NOT NULL,
    notes TEXT,
    resolved_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_violation
        FOREIGN KEY(violation_id)
        REFERENCES compliance.violations(violation_id)
        ON DELETE CASCADE
);

-- Table for complaints
CREATE TABLE IF NOT EXISTS compliance.complaints (
    complaint_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT,
    complaint_type VARCHAR(50) NOT NULL CHECK (complaint_type IN ('PRODUCT_QUALITY', 'PRICING_ISSUE', 'MISLEADING_INFORMATION', 'PACKAGING_PROBLEM', 'LEGAL_METROLOGY_VIOLATION', 'FAKE_PRODUCT', 'OTHER')),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    contact_info VARCHAR(255),
    evidence_file_path VARCHAR(500),
    priority VARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'CLOSED')),
    admin_response TEXT,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_complaint_user
        FOREIGN KEY(user_id)
        REFERENCES backend.users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_complaint_product
        FOREIGN KEY(product_id)
        REFERENCES compliance.products(product_id)
        ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON backend.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON backend.users(email);
CREATE INDEX IF NOT EXISTS idx_ocr_scan_results_user_id ON ocr_service.ocr_scan_results(user_id);
CREATE INDEX IF NOT EXISTS idx_cv_scan_results_user_id ON cv_service.cv_scan_results(user_id);
CREATE INDEX IF NOT EXISTS idx_violations_product_id ON compliance.violations(product_id);
CREATE INDEX IF NOT EXISTS idx_violations_status ON compliance.violations(status);
CREATE INDEX IF NOT EXISTS idx_products_name ON compliance.products(name);
CREATE INDEX IF NOT EXISTS idx_complaints_user_id ON compliance.complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON compliance.complaints(status);
CREATE INDEX IF NOT EXISTS idx_complaints_type ON compliance.complaints(complaint_type);
CREATE INDEX IF NOT EXISTS idx_complaints_priority ON compliance.complaints(priority);
CREATE INDEX IF NOT EXISTS idx_complaints_created_at ON compliance.complaints(created_at);

-- Insert sample data for testing
-- Sample admin user (password: admin123)
INSERT INTO backend.users (username, email, password) VALUES 
    ('admin', 'admin@shopshield.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi')
ON CONFLICT (username) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO backend.user_roles (user_id, role_id) 
SELECT u.id, r.id FROM backend.users u, backend.roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Sample products
INSERT INTO compliance.products (name, description, weight, price, packaging_info) VALUES 
    ('Organic Rice 1kg', 'Premium organic basmati rice', 1000.00, 250.00, 'Sealed plastic bag with cardboard outer packaging'),
    ('Cooking Oil 500ml', 'Refined sunflower oil', 500.00, 120.00, 'Plastic bottle with tamper-proof cap'),
    ('Instant Noodles', 'Vegetable flavor instant noodles', 75.00, 15.00, 'Individual sealed packet')
ON CONFLICT DO NOTHING;

-- Sample violations for testing
INSERT INTO compliance.violations (product_id, violated_rule_code, violated_rule_description) 
SELECT p.product_id, 'LM001', 'Missing mandatory weight declaration on package'
FROM compliance.products p 
WHERE p.name = 'Organic Rice 1kg'
ON CONFLICT DO NOTHING;

-- Sample complaints for testing
INSERT INTO compliance.complaints (user_id, product_id, complaint_type, title, description, contact_info, priority) 
SELECT u.id, p.product_id, 'PRODUCT_QUALITY', 'Poor quality rice received', 'The rice package I received contains broken grains and foreign particles. This does not match the product description.', 'customer@example.com', 'HIGH'
FROM backend.users u, compliance.products p 
WHERE u.username = 'admin' AND p.name = 'Organic Rice 1kg'
ON CONFLICT DO NOTHING;

INSERT INTO compliance.complaints (user_id, complaint_type, title, description, contact_info, priority) 
SELECT u.id, 'PRICING_ISSUE', 'Misleading price display', 'The displayed price was different from the charged amount at checkout. This seems like a pricing error.', 'concerned.buyer@example.com', 'MEDIUM'
FROM backend.users u 
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- Add comments on tables
COMMENT ON TABLE backend.users IS 'Stores user information for authentication and authorization.';
COMMENT ON TABLE backend.roles IS 'Stores available roles in the system.';
COMMENT ON TABLE backend.user_roles IS 'Maps users to their assigned roles.';
COMMENT ON TABLE ocr_service.ocr_scan_results IS 'Stores the raw text extracted from product images.';
COMMENT ON TABLE cv_service.cv_scan_results IS 'Stores results from the fake product detection analysis.';
COMMENT ON TABLE compliance.products IS 'Holds product information relevant for legal metrology compliance checks.';
COMMENT ON TABLE compliance.violations IS 'Records any detected violations of the Legal Metrology Act for a given product.';
COMMENT ON TABLE compliance.resolutions IS 'Tracks the resolution process for a specific violation.';
COMMENT ON TABLE compliance.complaints IS 'Stores user complaints about products, services, or compliance issues.';