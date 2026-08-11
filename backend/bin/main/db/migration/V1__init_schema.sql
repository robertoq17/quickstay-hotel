CREATE SCHEMA IF NOT EXISTS quickstay_write;

CREATE TABLE quickstay_write.hotels (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    ownership_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE quickstay_write.rooms (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL REFERENCES quickstay_write.hotels(id),
    room_type VARCHAR(80) NOT NULL,
    price_per_night NUMERIC(10,2) NOT NULL,
    capacity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE quickstay_write.guests (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(40),
    loyalty_points INT NOT NULL DEFAULT 0
);

CREATE TABLE quickstay_write.reservations (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES quickstay_write.rooms(id),
    guest_id UUID NOT NULL REFERENCES quickstay_write.guests(id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_rooms_hotel ON quickstay_write.rooms(hotel_id);
CREATE INDEX idx_reservations_room ON quickstay_write.reservations(room_id);
CREATE INDEX idx_reservations_guest ON quickstay_write.reservations(guest_id);
