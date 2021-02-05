CREATE TABLE profile_complaint (
  profile_id uuid NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),

  type text,
  content jsonb,

  PRIMARY KEY (profile_id, created_at)
);

ALTER TABLE profile_complaint
  ALTER COLUMN type SET STORAGE external,
  ALTER COLUMN content SET STORAGE external;
