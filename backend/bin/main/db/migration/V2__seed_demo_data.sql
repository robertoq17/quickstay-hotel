INSERT INTO quickstay_write.hotels (id, name, city, country, ownership_type, active) VALUES
    ('11111111-1111-1111-1111-111111111111', 'QuickStay Santa Cruz Centro', 'Santa Cruz de la Sierra', 'Bolivia', 'COMPANY_OWNED', true),
    ('22222222-2222-2222-2222-222222222222', 'QuickStay La Paz Plaza', 'La Paz', 'Bolivia', 'FRANCHISE', true);

INSERT INTO quickstay_write.rooms (id, hotel_id, room_type, price_per_night, capacity, active) VALUES
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Standard', 350.00, 2, true),
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Deluxe', 550.00, 3, true),
    ('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'Standard', 300.00, 2, true);
