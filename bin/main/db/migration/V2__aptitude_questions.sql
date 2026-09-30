CREATE TABLE IF NOT EXISTS aptitude_questions (
    id VARCHAR(36) PRIMARY KEY,
    test_id VARCHAR(36) NOT NULL REFERENCES aptitude_tests(id) ON DELETE CASCADE,
    prompt VARCHAR(1000) NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NOT NULL,
    option_d VARCHAR(500) NOT NULL,
    correct_option CHAR(1) NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    marks DOUBLE PRECISION NOT NULL CHECK (marks > 0)
);

CREATE INDEX IF NOT EXISTS idx_aptitude_question_test ON aptitude_questions(test_id);