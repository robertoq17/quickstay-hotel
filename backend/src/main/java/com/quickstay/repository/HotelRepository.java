package com.quickstay.repository;

import com.quickstay.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    List<Hotel> findByCityIgnoreCaseAndActiveTrue(String city);
}
