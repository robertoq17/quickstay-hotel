CREATE TABLE payments (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL UNIQUE,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payments_reservation ON payments(reservation_id);
