ALTER TABLE aptitude_scores
    ADD COLUMN IF NOT EXISTS completed_attempt BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS attempt_earned_marks DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS attempt_total_marks DOUBLE PRECISION;

CREATE UNIQUE INDEX IF NOT EXISTS uq_aptitude_score_completed_attempt
    ON aptitude_scores(student_id, test_id)
    WHERE completed_attempt = TRUE;

CREATE TABLE IF NOT EXISTS aptitude_attempt_answers (
    id VARCHAR(36) PRIMARY KEY,
    score_id VARCHAR(36) NOT NULL REFERENCES aptitude_scores(id) ON DELETE CASCADE,
    question_id VARCHAR(36) NOT NULL,
    question_order INTEGER NOT NULL,
    prompt VARCHAR(1000) NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NOT NULL,
    option_d VARCHAR(500) NOT NULL,
    selected_option CHAR(1) NOT NULL CHECK (selected_option IN ('A', 'B', 'C', 'D')),
    correct_option CHAR(1) NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    marks DOUBLE PRECISION NOT NULL CHECK (marks > 0),
    CONSTRAINT uq_attempt_answer_question UNIQUE(score_id, question_id)
);