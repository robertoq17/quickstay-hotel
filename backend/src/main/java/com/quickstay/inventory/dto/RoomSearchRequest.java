package com.quickstay.inventory.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomSearchRequest(
        @NotBlank String city,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut,
        @NotNull BigDecimal maxPrice
) {
}
