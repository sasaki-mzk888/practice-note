CREATE TABLE IF NOT EXISTS practice_records (
    practice_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    practice_date TEXT    NOT NULL,
    content       TEXT    NOT NULL CHECK (length(content) <= 200),
    duration_min  INTEGER NOT NULL CHECK (duration_min BETWEEN 1 AND 999),
    reflection    TEXT    CHECK (reflection IS NULL OR length(reflection) <= 500),
    next_goal     TEXT    CHECK (next_goal IS NULL OR length(next_goal) <= 200),
    created_at    TEXT    NOT NULL,
    updated_at    TEXT    NOT NULL
);
