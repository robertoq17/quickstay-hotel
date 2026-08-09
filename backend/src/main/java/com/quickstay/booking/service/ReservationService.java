package com.quickstay.booking.service;

import com.quickstay.booking.domain.Guest;
import com.quickstay.booking.domain.Reservation;
import com.quickstay.booking.domain.ReservationStatus;
import com.quickstay.booking.dto.ReservationRequest;
import com.quickstay.booking.dto.ReservationResponse;
import com.quickstay.booking.exception.RoomNotAvailableException;
import com.quickstay.booking.repository.GuestRepository;
import com.quickstay.booking.repository.ReservationRepository;
import com.quickstay.inventory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Booking domain service.
 *
 * Nota de diseño (Sesión III — Bounded Contexts):
 * Este servicio depende de {@link RoomRepository}, que pertenece al dominio
 * Inventory. Es el ÚNICO punto de acoplamiento entre Booking e Inventory que
 * queda tras el refactor, y es intencional: Booking necesita confirmar que
 * la Room existe antes de reservar. La dependencia es hacia un repositorio
 * (contrato de lectura), NO hacia un mapeo JPA de la entidad Room dentro del
 * agregado Reservation — así que si en una sesión futura Inventory se separa
 * en su propio microservicio (Sesión VI+), este punto se reemplaza por una
 * llamada HTTP/evento sin tocar el modelo de datos de Booking.
 * Ver docs/session-03-evaluation.md.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        boolean roomExists = roomRepository.existsById(request.roomId());
        if (!roomExists) {
            throw new NoSuchElementException("Room not found: " + request.roomId());
        }

        boolean overlapping = reservationRepository.existsOverlapping(
                request.roomId(), request.checkIn(), request.checkOut());

        if (overlapping) {
            throw new RoomNotAvailableException(
                    "Room " + request.roomId() + " is not available between "
                            + request.checkIn() + " and " + request.checkOut());
        }

        Guest guest = guestRepository.findByEmailIgnoreCase(request.guestEmail())
                .orElseGet(() -> createGuest(request));

        Reservation reservation = new Reservation();
        reservation.setRoomId(request.roomId());
        reservation.setGuest(guest);
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(Instant.now());

        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse cancel(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED);
        return toResponse(reservationRepository.save(reservation));
    }

    private Guest createGuest(ReservationRequest request) {
        Guest guest = new Guest();
        guest.setFullName(request.guestFullName());
        guest.setEmail(request.guestEmail());
        return guestRepository.save(guest);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoomId(),
                reservation.getGuest().getEmail(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getStatus()
        );
    }
}
