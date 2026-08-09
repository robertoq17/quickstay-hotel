package com.quickstay.repository;

import com.quickstay.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.room.id = :roomId
        AND r.status = 'CONFIRMED'
        AND r.checkIn < :checkOut AND r.checkOut > :checkIn
        """)
    boolean existsOverlapping(
            @Param("roomId") UUID roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    List<Reservation> findByGuestId(UUID guestId);
}
