package com.quickstay.booking.service;

import com.quickstay.booking.domain.Guest;
import com.quickstay.booking.domain.Reservation;
import com.quickstay.booking.domain.ReservationStatus;
import com.quickstay.booking.dto.ReservationRequest;
import com.quickstay.booking.dto.ReservationResponse;
import com.quickstay.booking.event.ReservationCancelledEvent;
import com.quickstay.booking.event.ReservationConfirmedEvent;
import com.quickstay.booking.exception.RoomNotAvailableException;
import com.quickstay.booking.repository.GuestRepository;
import com.quickstay.booking.repository.ReservationRepository;
import com.quickstay.inventory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
 * Inventory. Es el ÚNICO punto de acoplamiento SÍNCRONO entre Booking e
 * Inventory que queda tras el refactor de Sesión III, y es intencional.
 *
 * Nota de diseño (Sesión IV — Enterprise Integration & Messaging):
 * Al confirmar o cancelar una reserva, este servicio publica domain events
 * en memoria vía {@link ApplicationEventPublisher} (el "in-memory event
 * bus" de la Actividad 2). El servicio NO sabe que existe RabbitMQ ni
 * ningún broker — esa traducción ocurre en la capa de infraestructura
 * ({@code booking.messaging.ReservationEventPublisher}), que escucha estos
 * eventos después de que la transacción confirma (AFTER_COMMIT) y recién
 * ahí los reenvía como mensajes externos. Así, la comunicación con
 * Notification pasa de ser síncrona/acoplada a asíncrona/desacoplada, con
 * consistencia eventual: el llamador de reserve() recibe su 201 Created
 * inmediatamente, sin esperar a que la notificación se envíe.
 * Ver docs/session-04-evaluation.md.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        eventPublisher.publishEvent(new ReservationConfirmedEvent(
                saved.getId(),
                saved.getRoomId(),
                guest.getFullName(),
                guest.getEmail(),
                saved.getCheckIn(),
                saved.getCheckOut()
        ));

        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse cancel(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        eventPublisher.publishEvent(new ReservationCancelledEvent(
                saved.getId(),
                saved.getRoomId(),
                saved.getGuest().getEmail()
        ));

        return toResponse(saved);
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
