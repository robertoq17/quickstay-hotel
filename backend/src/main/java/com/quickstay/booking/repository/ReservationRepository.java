package com.quickstay.booking.repository;

import com.quickstay.booking.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    /**
     * Nota de diseño (Sesión III): esta query ya no hace join a la tabla
     * "rooms" (dominio Inventory) — solo filtra por la columna plana
     * room_id. El dominio Booking no conoce la estructura interna de Room,
     * solo su identificador.
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.roomId = :roomId
        AND r.status IN ('PENDING_PAYMENT', 'CONFIRMED')
        AND r.checkIn < :checkOut AND r.checkOut > :checkIn
        """)
    boolean existsOverlapping(
            @Param("roomId") UUID roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    List<Reservation> findByGuestId(UUID guestId);

    List<Reservation> findByRoomId(UUID roomId);
}
