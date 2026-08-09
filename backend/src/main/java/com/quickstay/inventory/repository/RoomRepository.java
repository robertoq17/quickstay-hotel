package com.quickstay.inventory.repository;

import com.quickstay.inventory.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Nota de diseño (Sesión III — acoplamiento pendiente):
     * Esta query del dominio Inventory sigue referenciando la entidad
     * Reservation, que pertenece al dominio Booking. Es el acoplamiento
     * cross-domain más fuerte que queda tras el refactor de esta sesión:
     * Inventory necesita saber qué habitaciones tienen reservas activas
     * para calcular disponibilidad, y ambas tablas comparten la misma DB.
     *
     * Se documenta como decisión consciente, no como descuido: resolverlo
     * del todo (ej. que Inventory mantenga su propia proyección de
     * disponibilidad, actualizada por eventos de Booking) es precisamente
     * el tipo de cambio que corresponde a la Sesión IV (Enterprise
     * Integration & Messaging) y a la Sesión VIII (CQRS). Ver
     * docs/session-03-evaluation.md.
     */
    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.city = :city
        AND r.active = true
        AND r.pricePerNight <= :maxPrice
        AND r.id NOT IN (
            SELECT res.roomId FROM Reservation res
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
