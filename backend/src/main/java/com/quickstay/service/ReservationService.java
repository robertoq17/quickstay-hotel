package com.quickstay.service;

import com.quickstay.domain.Guest;
import com.quickstay.domain.Reservation;
import com.quickstay.domain.ReservationStatus;
import com.quickstay.domain.Room;
import com.quickstay.dto.ReservationRequest;
import com.quickstay.dto.ReservationResponse;
import com.quickstay.exception.RoomNotAvailableException;
import com.quickstay.repository.GuestRepository;
import com.quickstay.repository.ReservationRepository;
import com.quickstay.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new NoSuchElementException("Room not found: " + request.roomId()));

        boolean overlapping = reservationRepository.existsOverlapping(
                room.getId(), request.checkIn(), request.checkOut());

        if (overlapping) {
            throw new RoomNotAvailableException(
                    "Room " + room.getId() + " is not available between "
                            + request.checkIn() + " and " + request.checkOut());
        }

        Guest guest = guestRepository.findByEmailIgnoreCase(request.guestEmail())
                .orElseGet(() -> createGuest(request));

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
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
                reservation.getRoom().getId(),
                reservation.getGuest().getEmail(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getStatus()
        );
    }
}
