INSERT INTO credit_segments (uuid, ssn, type, credit_modifier)
VALUES ('deb43f7d-7c4a-4c4b-8a48-5c5ebdf34b02', '49002010965', 'DEBT', 666)
ON CONFLICT(ssn) DO UPDATE SET
  type = excluded.type,
  credit_modifier = excluded.credit_modifier,
  uuid = excluded.uuid;

INSERT INTO credit_segments (uuid, ssn, type, credit_modifier)
VALUES ('05c1f9f8-be09-4aa8-81ae-fc946ada1c1e', '49002010976', 'SEGMENT_1', 100)
ON CONFLICT(ssn) DO UPDATE SET
  type = excluded.type,
  credit_modifier = excluded.credit_modifier,
  uuid = excluded.uuid;

INSERT INTO credit_segments (uuid, ssn, type, credit_modifier)
VALUES ('8eb7bb2d-5d67-44af-a28a-8c0a7300da4f', '49002010987', 'SEGMENT_2', 300)
ON CONFLICT(ssn) DO UPDATE SET
  type = excluded.type,
  credit_modifier = excluded.credit_modifier,
  uuid = excluded.uuid;

INSERT INTO credit_segments (uuid, ssn, type, credit_modifier)
VALUES ('3ba16aa5-7f0e-4f5c-9d7d-91d8e1b2c71f', '49002010998', 'SEGMENT_3', 1000)
ON CONFLICT(ssn) DO UPDATE SET
  type = excluded.type,
  credit_modifier = excluded.credit_modifier,
  uuid = excluded.uuid;
