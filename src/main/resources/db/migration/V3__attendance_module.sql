CREATE TABLE absences
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    absence_date     DATE        NOT NULL,
    absence_type     VARCHAR(30) NOT NULL
        CONSTRAINT chk_absence_type CHECK (
            absence_type IN (
                             'JUSTIFIED_LEAVE',
                             'JUSTIFIED_JUSTIFICATION',
                             'PENDING_JUSTIFICATION',
                             'UNJUSTIFIED',
                             'UNJUSTIFIED_FINAL'
                )
            ),
    is_final         BOOLEAN     NOT NULL DEFAULT FALSE,
    consecutive_days INT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ,

    CONSTRAINT fk_absences_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT unique_user_absence_date
        UNIQUE (user_id, absence_date)
);

CREATE TABLE absence_alerts
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    alert_level      VARCHAR(10) NOT NULL
        CONSTRAINT chk_alert_level CHECK (
            alert_level IN ('RH', 'CEO')
            ),
    triggered_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consecutive_days INT         NOT NULL,

    CONSTRAINT fk_absence_alerts_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- ===============================
-- INDEX (performance)
-- ===============================
CREATE INDEX idx_absences_user_date
    ON absences (user_id, absence_date);

CREATE INDEX idx_absence_alerts_user_level
    ON absence_alerts (user_id, alert_level);