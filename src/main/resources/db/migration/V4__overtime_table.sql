CREATE TABLE overtimes (
                           id uuid PRIMARY KEY,
                           working_day_id uuid NOT NULL,
                           user_id uuid NOT NULL,
                           hours_25 double precision DEFAULT 0,
                           hours_50 double precision DEFAULT 0,
                           hours_100 double precision DEFAULT 0,
                           status VARCHAR(20) CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                           manager_comment TEXT NULL,
                           approved_by uuid,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_ot_working_day FOREIGN KEY (working_day_id) REFERENCES working_days (id),
                           CONSTRAINT fk_ot_user FOREIGN KEY (user_id) REFERENCES users (id)
);