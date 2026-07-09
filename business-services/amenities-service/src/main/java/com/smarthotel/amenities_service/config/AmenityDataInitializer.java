package com.smarthotel.amenities_service.config;

import com.smarthotel.amenities_service.entity.Amenity;
import com.smarthotel.amenities_service.entity.AmenityStatus;
import com.smarthotel.amenities_service.repository.AmenityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmenityDataInitializer implements CommandLineRunner {

    private final AmenityRepository amenityRepository;

    public AmenityDataInitializer(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Override
    public void run(String... args) {

        if (amenityRepository.count() > 0) {
            return;
        }

        // FOOD
        createAmenity("Nước suối", "FOOD", "10", true, AmenityStatus.AVAILABLE);
        createAmenity("Coca Cola", "FOOD", "15", true, AmenityStatus.AVAILABLE);
        createAmenity("Pepsi", "FOOD", "15", true, AmenityStatus.AVAILABLE);
        createAmenity("Mì ly", "FOOD", "10", true, AmenityStatus.OUT_OF_STOCK);
        createAmenity("Bánh mì sandwich", "FOOD", "25", true, AmenityStatus.AVAILABLE);
        createAmenity("Khoai tây chiên", "FOOD", "25", true, AmenityStatus.AVAILABLE);

        // LAUNDRY
        createAmenity("Giặt áo", "LAUNDRY", "10", false, AmenityStatus.AVAILABLE);
        createAmenity("Giặt quần", "LAUNDRY", "15", false, AmenityStatus.AVAILABLE);
        createAmenity("Giặt vest", "LAUNDRY", "20", false, AmenityStatus.AVAILABLE);
        createAmenity("Giặt khô", "LAUNDRY", "30", false, AmenityStatus.OUT_OF_STOCK);

        // SPA
        createAmenity("Massage 60 phút", "SPA", "200", false, AmenityStatus.AVAILABLE);
        createAmenity("Massage 90 phút", "SPA", "280", false, AmenityStatus.AVAILABLE);
        createAmenity("Chăm sóc da mặt", "SPA", "150", false, AmenityStatus.OUT_OF_STOCK);
        createAmenity("Xông hơi", "SPA", "100", false, AmenityStatus.AVAILABLE);

        System.out.println("Default amenities seeded successfully.");
    }

    private void createAmenity(
            String name,
            String type,
            String price,
            Boolean isReturnable,
            AmenityStatus status
    ) {

        Amenity amenity = Amenity.builder()
                .name(name)
                .type(type)
                .price(new BigDecimal(price))
                .isReturnable(isReturnable)
                .status(status)
                .build();

        amenityRepository.save(amenity);
    }
}