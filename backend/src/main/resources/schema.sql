CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    user_name     VARCHAR(80)  NOT NULL,
    role          VARCHAR(16)  NOT NULL DEFAULT 'EDITOR',
    created_at    DATETIME(6)  NOT NULL
);

CREATE TABLE IF NOT EXISTS practice_records (
    practice_id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    practice_date DATE         NOT NULL,
    content       VARCHAR(200) NOT NULL,
    duration_min  INT          NOT NULL,
    reflection    VARCHAR(500),
    next_goal     VARCHAR(200),
    user_id       BIGINT,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT chk_practice_duration CHECK (duration_min BETWEEN 1 AND 999),
    CONSTRAINT fk_practice_records_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE SET NULL
);
