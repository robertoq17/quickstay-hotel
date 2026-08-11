CREATE TABLE quickstay_write.payments (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL UNIQUE REFERENCES quickstay_write.reservations(id),
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE quickstay_write.saga_executions (
    id UUID PRIMARY KEY,
    reservation_id UUID UNIQUE REFERENCES quickstay_write.reservations(id),
    status VARCHAR(40) NOT NULL,
    current_step VARCHAR(80) NOT NULL,
    error_message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_saga_status ON quickstay_write.saga_executions(status);
CREATE INDEX idx_payments_reservation ON quickstay_write.payments(reservation_id);
