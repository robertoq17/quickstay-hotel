package com.quickstay.booking.repository;

import com.quickstay.booking.domain.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {

    Optional<Guest> findByEmailIgnoreCase(String email);
}
