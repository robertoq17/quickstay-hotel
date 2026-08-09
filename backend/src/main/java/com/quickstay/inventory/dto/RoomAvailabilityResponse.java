package com.quickstay.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomAvailabilityResponse(
        UUID roomId,
        String hotelName,
        String city,
        String roomType,
        BigDecimal pricePerNight,
        int capacity
) {
}
