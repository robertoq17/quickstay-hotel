-- Session VIII: CQRS read side.
-- WRITE and READ persistence are explicitly separated by PostgreSQL schemas.
CREATE SCHEMA IF NOT EXISTS quickstay_read;

CREATE TABLE quickstay_read.room_availability_read_model (
    room_id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    hotel_name VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    room_type VARCHAR(80) NOT NULL,
    price_per_night NUMERIC(10,2) NOT NULL,
    capacity INT NOT NULL,
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quickstay_read.reservation_availability_read_model (
    reservation_id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_room_read_city_price
    ON quickstay_read.room_availability_read_model(city, price_per_night);

CREATE INDEX idx_reservation_read_room_dates
    ON quickstay_read.reservation_availability_read_model(room_id, check_in, check_out);

CREATE INDEX idx_reservation_read_status
    ON quickstay_read.reservation_availability_read_model(status);

-- Bootstrap the READ projection from the current WRITE model.
INSERT INTO quickstay_read.room_availability_read_model
    (room_id, hotel_id, hotel_name, city, room_type, price_per_night, capacity, active)
SELECT r.id, h.id, h.name, h.city, r.room_type, r.price_per_night, r.capacity, r.active
FROM quickstay_write.rooms r
JOIN quickstay_write.hotels h ON h.id = r.hotel_id;

INSERT INTO quickstay_read.reservation_availability_read_model
    (reservation_id, room_id, check_in, check_out, status)
SELECT id, room_id, check_in, check_out, status
FROM quickstay_write.reservations;
