CREATE TABLE rfid_cards
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uid         VARCHAR(50)  UNIQUE NOT NULL,
    employee_id UUID               NOT NULL,
    enrolled_at TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    is_active   BOOLEAN            NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_rfid_cards_employee
        FOREIGN KEY (employee_id)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);