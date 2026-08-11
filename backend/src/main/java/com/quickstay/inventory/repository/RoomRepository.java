package com.quickstay.inventory.repository;

import com.quickstay.inventory.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

}
