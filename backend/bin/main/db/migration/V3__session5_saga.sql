CREATE TABLE payments (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL UNIQUE REFERENCES reservations(id),
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE saga_executions (
    id UUID PRIMARY KEY,
    reservation_id UUID UNIQUE REFERENCES reservations(id),
    status VARCHAR(40) NOT NULL,
    current_step VARCHAR(80) NOT NULL,
    error_message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_saga_status ON saga_executions(status);
CREATE INDEX idx_payments_reservation ON payments(reservation_id);
