-- Sample records for a new database. Add records only while the table is empty.
INSERT INTO practice_records (practice_date, content, duration_min, reflection, next_goal, created_at, updated_at)
SELECT DATE '2026-11-04', 'サーブ練習、ストローク', 90, 'ファーストサーブの確率が低い', 'トスを安定させる', TIMESTAMP '2026-11-04 20:00:00', TIMESTAMP '2026-11-04 20:00:00'
WHERE NOT EXISTS (SELECT 1 FROM practice_records);

INSERT INTO practice_records (practice_date, content, duration_min, reflection, next_goal, created_at, updated_at)
SELECT DATE '2026-11-03', 'ボレー練習、ゲーム練習', 120, 'ネット前の判断が遅れた', '一歩目を早くする', TIMESTAMP '2026-11-03 20:00:00', TIMESTAMP '2026-11-03 20:00:00'
WHERE (SELECT COUNT(*) FROM practice_records) = 1;

INSERT INTO practice_records (practice_date, content, duration_min, reflection, next_goal, created_at, updated_at)
SELECT DATE '2026-11-02', '基礎練習', 60, NULL, NULL, TIMESTAMP '2026-11-02 20:00:00', TIMESTAMP '2026-11-02 20:00:00'
WHERE (SELECT COUNT(*) FROM practice_records) = 2;
