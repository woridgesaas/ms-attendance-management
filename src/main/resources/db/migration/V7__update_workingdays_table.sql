ALTER TABLE working_days
DROP CONSTRAINT IF EXISTS fk_working_days_approved_by;

-- All these fields are handlled now in table overtimes
ALTER TABLE working_days
DROP COLUMN IF EXISTS is_overtime,
DROP COLUMN IF EXISTS approved_by;