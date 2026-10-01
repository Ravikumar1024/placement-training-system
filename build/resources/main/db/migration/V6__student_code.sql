ALTER TABLE students
    ADD COLUMN IF NOT EXISTS code VARCHAR(50);

UPDATE students student
SET code = COALESCE(account.username, 'STU-' || student.id)
FROM users account
WHERE student.user_id = account.id
  AND student.code IS NULL;

UPDATE students
SET code = 'STU-' || id
WHERE code IS NULL;

ALTER TABLE students
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_students_code
    ON students(code);