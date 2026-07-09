package com.smarthotel.room_service.config;

import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.room_service.entity.Room;
import com.smarthotel.room_service.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RoomDataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    public RoomDataInitializer(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {

        if (roomRepository.count() > 0) {
            return;
        }

        createRoom("101", "SINGLE", new BigDecimal("80.00"), 1);
        createRoom("102", "SINGLE", new BigDecimal("80.00"), 1);
        createRoom("103", "SINGLE", new BigDecimal("80.00"), 1);
        createRoom("104", "SINGLE", new BigDecimal("80.00"), 1);
        createRoom("105", "SINGLE", new BigDecimal("80.00"), 1);

        createRoom("201", "DOUBLE", new BigDecimal("120.00"), 2);
        createRoom("202", "DOUBLE", new BigDecimal("120.00"), 2);
        createRoom("203", "DOUBLE", new BigDecimal("120.00"), 2);
        createRoom("204", "DOUBLE", new BigDecimal("120.00"), 2);
        createRoom("205", "DOUBLE", new BigDecimal("120.00"), 2);

        createRoom("301", "SUITE", new BigDecimal("250.00"), 3);
        createRoom("302", "SUITE", new BigDecimal("250.00"), 3);
        createRoom("303", "SUITE", new BigDecimal("250.00"), 3);
        createRoom("304", "SUITE", new BigDecimal("250.00"), 3);
        createRoom("305", "SUITE", new BigDecimal("250.00"), 3);
    }

    private void createRoom(String roomNumber, String roomType, BigDecimal price, int floor) {
        Room room = Room.builder()
                .roomNumber(roomNumber)
                .roomType(roomType)
                .status(RoomStatus.AVAILABLE)
                .basePrice(price)
                .floor(floor)
                .reservedBookingId(null)
                .build();

        roomRepository.save(room);
    }
}