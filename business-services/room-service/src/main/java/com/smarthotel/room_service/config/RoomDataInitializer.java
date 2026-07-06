package com.smarthotel.room_service.config;

import com.smarthotel.room_service.entity.Room;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.room_service.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class RoomDataInitializer implements CommandLineRunner {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            Room r101 = Room.builder()
                    .roomNumber("101")
                    .roomType("SINGLE")
                    .status(RoomStatus.AVAILABLE)
                    .basePrice(new BigDecimal("100.00"))
                    .floor(1)
                    .build();

            Room r102 = Room.builder()
                    .roomNumber("102")
                    .roomType("DOUBLE")
                    .status(RoomStatus.AVAILABLE)
                    .basePrice(new BigDecimal("150.00"))
                    .floor(1)
                    .build();

            Room r201 = Room.builder()
                    .roomNumber("201")
                    .roomType("DOUBLE")
                    .status(RoomStatus.AVAILABLE)
                    .basePrice(new BigDecimal("180.00"))
                    .floor(2)
                    .build();

            Room r202 = Room.builder()
                    .roomNumber("202")
                    .roomType("SUITE")
                    .status(RoomStatus.AVAILABLE)
                    .basePrice(new BigDecimal("300.00"))
                    .floor(2)
                    .build();

            roomRepository.save(r101);
            roomRepository.save(r102);
            roomRepository.save(r201);
            roomRepository.save(r202);
            System.out.println("Default rooms seeded successfully in hotel_room_db");
        }
    }
}
