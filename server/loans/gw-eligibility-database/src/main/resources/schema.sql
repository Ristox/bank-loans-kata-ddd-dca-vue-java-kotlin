CREATE TABLE IF NOT EXISTS credit_segments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  uuid TEXT NOT NULL UNIQUE,
  ssn TEXT NOT NULL UNIQUE,
  type TEXT NOT NULL CHECK (type IN ('SEGMENT_1', 'SEGMENT_2', 'SEGMENT_3', 'DEBT')),
  credit_modifier INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_credit_segments_ssn ON credit_segments (ssn);
