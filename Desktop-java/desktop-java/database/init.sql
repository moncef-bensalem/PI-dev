-- NEXUS Desktop Application Database Schema
-- Create database
CREATE DATABASE IF NOT EXISTS nexus_desktop;
USE nexus_desktop;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    roles TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Candidates table
CREATE TABLE IF NOT EXISTS candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    skills TEXT,
    experience_years INT,
    status ENUM('active', 'inactive', 'pending') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,
    location VARCHAR(100),
    salary_range VARCHAR(100),
    status ENUM('open', 'closed', 'draft') DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Applications table (link between candidates and jobs)
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status ENUM('applied', 'interview', 'rejected', 'hired') DEFAULT 'applied',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_application (candidate_id, job_id)
);

-- Insert sample data
INSERT INTO users (email, first_name, last_name, password_hash, roles, active) VALUES
('admin@nexus.com', 'Admin', 'User', '978732737', 'admin', TRUE),
('recruiter@nexus.com', 'Recruiter', 'User', '978732738', 'recruiter', TRUE);

INSERT INTO candidates (first_name, last_name, email, phone, skills, experience_years, status) VALUES
('John', 'Doe', 'john.doe@email.com', '+1234567890', 'Java,Python,JavaScript', 5, 'active'),
('Jane', 'Smith', 'jane.smith@email.com', '+0987654321', 'React,Node.js,SQL', 3, 'active');

INSERT INTO jobs (title, description, requirements, location, salary_range, status) VALUES
('Senior Java Developer', 'We are looking for an experienced Java developer', '5+ years Java experience, Spring Framework', 'New York', '$80,000-$120,000', 'open'),
('Frontend Developer', 'React developer needed for exciting projects', '3+ years React experience, JavaScript', 'San Francisco', '$70,000-$100,000', 'open');

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_applications_candidate ON applications(candidate_id);
CREATE INDEX idx_applications_job ON applications(job_id);