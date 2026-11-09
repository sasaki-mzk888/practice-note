package com.example.practicenote.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 既存DBにもユーザー所有者カラムと外部キーを追加する */
@Component
public class LegacySchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public LegacySchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer roleColumnCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'users'
                  AND COLUMN_NAME = 'role'
                """, Integer.class);

        if (roleColumnCount == null || roleColumnCount == 0) {
            jdbc.execute("ALTER TABLE users ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'EDITOR'");
        }

        Integer userIdColumnCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'practice_records'
                  AND COLUMN_NAME = 'user_id'
                """, Integer.class);

        if (userIdColumnCount == null || userIdColumnCount == 0) {
            jdbc.execute("ALTER TABLE practice_records ADD COLUMN user_id BIGINT NULL");
        }

        Integer foreignKeyCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'practice_records'
                  AND CONSTRAINT_NAME = 'fk_practice_records_user'
                """, Integer.class);

        if (foreignKeyCount == null || foreignKeyCount == 0) {
            jdbc.execute("""
                    ALTER TABLE practice_records
                    ADD CONSTRAINT fk_practice_records_user
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
                    """);
        }
    }
}
