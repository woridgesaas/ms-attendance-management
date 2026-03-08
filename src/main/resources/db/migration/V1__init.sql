-- User table
CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    CIN        VARCHAR(15) UNIQUE  NOT NULL,
    first_name VARCHAR(50)         NOT NULL,
    last_name  VARCHAR(50)         NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL
);

-- Leave request table
CREATE TABLE leave_requests
(
    id           uuid PRIMARY KEY,
    request_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date   DATE        NOT NULL,
    end_date     DATE        NOT NULL,
    leave_type   VARCHAR(50) NOT NULL,
    user_id      uuid        NOT NULL,

    CONSTRAINT fk_leave_requests_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT check_leave_dates CHECK (end_date > start_date)
);

-- Leave day table
CREATE TABLE leave_days
(
    id               uuid PRIMARY KEY,
    date_of_day      DATE NOT NULL,
    leave_request_id uuid NOT NULL,

    CONSTRAINT fk_leave_days_attendance_id
        FOREIGN KEY (leave_request_id)
            REFERENCES leave_requests (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- Working day table
CREATE TABLE working_days
(
    id             uuid PRIMARY KEY,
    date           DATE NOT NULL,
    check_in_time  TIME,
    check_out_time TIME,
    is_overtime    BOOLEAN DEFAULT FALSE,
    approved_by    uuid,
    performed_by   uuid NOT NULL,

    CONSTRAINT fk_working_days_performed_by
        FOREIGN KEY (performed_by)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_working_days_approved_by
        FOREIGN KEY (approved_by)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- Validation table
CREATE TABLE validations
(
    id               uuid PRIMARY KEY,
    decision         VARCHAR(20) CHECK (decision IN ('APPROVED', 'REJECTED', 'PENDING')),
    decision_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leave_request_id uuid      NOT NULL,
    performed_by     uuid      NOT NULL,

    CONSTRAINT fk_validations_leave_request_id
        FOREIGN KEY (leave_request_id)
            REFERENCES leave_requests (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_validations_performed_by
        FOREIGN KEY (performed_by)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- Justification table
CREATE TABLE justifications
(
    id               uuid PRIMARY KEY,
    submission_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    path             TEXT NOT NULL,
    leave_request_id uuid NOT NULL,

    CONSTRAINT fk_justifications_leave_request_id
        FOREIGN KEY (leave_request_id)
            REFERENCES leave_requests (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- History table
CREATE TABLE histories
(
    id               uuid PRIMARY KEY,
    action_date      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action           VARCHAR(50) NOT NULL,
    leave_request_id uuid        NOT NULL,

    CONSTRAINT fk_histories_leave_request_id
        FOREIGN KEY (leave_request_id)
            REFERENCES leave_requests (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

--  Balance table (Leave balance per year)
CREATE TABLE balances
(
    id                uuid PRIMARY KEY,
    year              INT  NOT NULL,
    initial_balance   INT  NOT NULL,
    remaining_balance INT  NOT NULL,
    user_id           uuid NOT NULL,

    CONSTRAINT fk_balances_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT unique_user_year UNIQUE (user_id, year)

);