package com.quickstay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationRequest(
        @NotNull UUID roomId,
        @NotBlank String guestFullName,
        @NotBlank @Email String guestEmail,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut
) {
}
