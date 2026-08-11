package com.quickstay.inventory.service;

import com.quickstay.inventory.dto.RoomAvailabilityResponse;
import com.quickstay.inventory.dto.RoomSearchRequest;
import com.quickstay.inventory.readmodel.RoomAvailabilityReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomSearchService {

    private final RoomAvailabilityReadRepository readRepository;

    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> search(RoomSearchRequest request) {
        return readRepository.search(
                request.city(),
                request.checkIn(),
                request.checkOut(),
                request.maxPrice()
        );
    }
}
