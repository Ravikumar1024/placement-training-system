CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(15),
    department VARCHAR(50),
    batch VARCHAR(10),
    cgpa DOUBLE PRECISION,
    backlogs INTEGER,
    skills VARCHAR(500),
    user_id VARCHAR(36) UNIQUE REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS companies (
    id VARCHAR(36) PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    job_role VARCHAR(100),
    package_lpa DOUBLE PRECISION,
    min_cgpa DOUBLE PRECISION,
    max_backlogs INTEGER,
    min_aptitude_score DOUBLE PRECISION,
    min_attendance DOUBLE PRECISION,
    eligible_departments VARCHAR(200),
    required_skills VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS trainings (
    id VARCHAR(36) PRIMARY KEY,
    training_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    trainer VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE
);

CREATE TABLE IF NOT EXISTS placements (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL REFERENCES students(id),
    company_id VARCHAR(36) NOT NULL REFERENCES companies(id),
    status VARCHAR(255),
    applied_date DATE,
    placement_date DATE
);

CREATE TABLE IF NOT EXISTS attendance (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL REFERENCES students(id),
    training_id VARCHAR(36) NOT NULL REFERENCES trainings(id),
    date DATE NOT NULL,
    present BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS aptitude_tests (
    id VARCHAR(36) PRIMARY KEY,
    test_name VARCHAR(255) NOT NULL,
    test_date DATE,
    total_marks DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS aptitude_scores (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL REFERENCES students(id),
    test_id VARCHAR(36) NOT NULL REFERENCES aptitude_tests(id),
    score DOUBLE PRECISION
);