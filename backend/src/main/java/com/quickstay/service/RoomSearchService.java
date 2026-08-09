package com.quickstay.service;

import com.quickstay.domain.Room;
import com.quickstay.dto.RoomAvailabilityResponse;
import com.quickstay.dto.RoomSearchRequest;
import com.quickstay.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomSearchService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> search(RoomSearchRequest request) {
        List<Room> rooms = roomRepository.findAvailableRooms(
                request.city(),
                request.checkIn(),
                request.checkOut(),
                request.maxPrice()
        );

        return rooms.stream()
                .map(this::toResponse)
                .toList();
    }

    private RoomAvailabilityResponse toResponse(Room room) {
        return new RoomAvailabilityResponse(
                room.getId(),
                room.getHotel().getName(),
                room.getHotel().getCity(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getCapacity()
        );
    }
}
