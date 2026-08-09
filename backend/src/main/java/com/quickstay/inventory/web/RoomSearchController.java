package com.quickstay.inventory.web;

import com.quickstay.inventory.dto.RoomAvailabilityResponse;
import com.quickstay.inventory.dto.RoomSearchRequest;
import com.quickstay.inventory.service.RoomSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomSearchController {

    private final RoomSearchService roomSearchService;

    @GetMapping("/api/rooms/search")
    public List<RoomAvailabilityResponse> search(
            @RequestParam String city,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam BigDecimal maxPrice
    ) {
        RoomSearchRequest request = new RoomSearchRequest(city, checkIn, checkOut, maxPrice);
        return roomSearchService.search(request);
    }
}
