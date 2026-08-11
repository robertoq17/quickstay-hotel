package com.quickstay.inventory.readmodel;

import com.quickstay.inventory.dto.RoomAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * CQRS read-side repository.
 *
 * The search use case deliberately does NOT read the JPA write model
 * (Room/Reservation entities). It queries the denormalized read model
 * maintained by Inventory's projection consumer.
 */
@Repository
@RequiredArgsConstructor
public class RoomAvailabilityReadRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<RoomAvailabilityResponse> search(
            String city,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal maxPrice
    ) {
        return jdbcTemplate.query("""
                SELECT r.room_id,
                       r.hotel_name,
                       r.city,
                       r.room_type,
                       r.price_per_night,
                       r.capacity
                FROM quickstay_read.room_availability_read_model r
                WHERE r.city = ?
                  AND r.active = true
                  AND r.price_per_night <= ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM quickstay_read.reservation_availability_read_model rr
                      WHERE rr.room_id = r.room_id
                        AND rr.status IN ('PENDING_PAYMENT', 'CONFIRMED')
                        AND rr.check_in < ?
                        AND rr.check_out > ?
                  )
                ORDER BY r.price_per_night, r.hotel_name, r.room_type
                """,
                (rs, rowNum) -> new RoomAvailabilityResponse(
                        rs.getObject("room_id", UUID.class),
                        rs.getString("hotel_name"),
                        rs.getString("city"),
                        rs.getString("room_type"),
                        rs.getBigDecimal("price_per_night"),
                        rs.getInt("capacity")
                ),
                city,
                maxPrice,
                checkOut,
                checkIn
        );
    }
}
