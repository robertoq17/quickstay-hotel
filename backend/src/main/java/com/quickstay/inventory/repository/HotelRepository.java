package com.quickstay.inventory.repository;

import com.quickstay.inventory.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    List<Hotel> findByCityIgnoreCaseAndActiveTrue(String city);
}
