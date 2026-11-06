CREATE TABLE IF NOT EXISTS practice_records (
    practice_id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    practice_date DATE         NOT NULL,
    content       VARCHAR(200) NOT NULL,
    duration_min  INT          NOT NULL,
    reflection    VARCHAR(500),
    next_goal     VARCHAR(200),
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT chk_practice_duration CHECK (duration_min BETWEEN 1 AND 999)
);
