package com.smarthotel.billing.client.fallback;

import com.smarthotel.billing.client.AmenityClient;
import com.smarthotel.billing.dto.response.UnpaidAmenityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Circuit-breaker fallback cho {@link AmenityClient}.
 * Suy giam muot ma: neu amenities-service loi, coi nhu khong co dich vu chua thanh toan
 * (serviceCharge = 0) de hoa don van tao duoc thay vi that bai hoan toan.
 */
@Slf4j
@Component
public class AmenityClientFallback implements AmenityClient {

    @Override
    public List<UnpaidAmenityDTO> getUnpaid(UUID roomId) {
        log.warn("[CircuitBreaker] amenities-service khong kha dung -> fallback getUnpaid({}) tra ve rong", roomId);
        return Collections.emptyList();
    }

    @Override
    public UnpaidAmenityDTO updateOrderStatus(UUID id, String status) {
        log.warn("[CircuitBreaker] amenities-service khong kha dung -> bo qua updateOrderStatus({}, {})", id, status);
        return null;
    }
}
