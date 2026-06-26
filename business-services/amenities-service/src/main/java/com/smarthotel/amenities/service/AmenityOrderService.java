package com.smarthotel.amenities.service;

import com.smarthotel.amenities.dto.request.AmenityOrderCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityOrderResponse;
import java.util.List;
import java.util.UUID;

public interface AmenityOrderService {
    AmenityOrderResponse createOrder(AmenityOrderCreateRequest request);
    List<AmenityOrderResponse> getUnpaidByRoomId(UUID roomId);
    AmenityOrderResponse updateOrderStatus(UUID id, String status);
}
