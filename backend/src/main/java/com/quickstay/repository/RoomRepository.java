package com.quickstay.repository;

import com.quickstay.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.city = :city
        AND r.active = true
        AND r.pricePerNight <= :maxPrice
        AND r.id NOT IN (
            SELECT res.room.id FROM Reservation res
            WHERE res.status = 'CONFIRMED'
            AND res.checkIn < :checkOut
            AND res.checkOut > :checkIn
        )
        """)
    List<Room> findAvailableRooms(
            @Param("city") String city,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
