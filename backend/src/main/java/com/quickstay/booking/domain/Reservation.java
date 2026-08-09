package com.quickstay.booking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Referencia por ID al agregado Room, que pertenece al dominio Inventory.
     * Deliberadamente NO es una relación JPA (@ManyToOne) hacia Room: en un
     * modular monolith, un dominio no debe mapear entidades de otro dominio
     * como parte de su propio grafo de persistencia. Esto preserva el límite
     * del Bounded Context, aunque ambas tablas convivan en la misma base de
     * datos física (ver docs/session-03-evaluation.md).
     */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant createdAt;
}
