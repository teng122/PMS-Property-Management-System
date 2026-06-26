package com.smarthotel.amenities.service.impl;

import com.smarthotel.amenities.dto.request.AmenityOrderCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityOrderResponse;
import com.smarthotel.amenities.entity.Amenity;
import com.smarthotel.amenities.entity.AmenityOrder;
import com.smarthotel.amenities.exception.ResourceNotFoundException;
import com.smarthotel.amenities.repository.AmenityOrderRepository;
import com.smarthotel.amenities.repository.AmenityRepository;
import com.smarthotel.amenities.service.AmenityOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AmenityOrderServiceImpl implements AmenityOrderService {

    @Autowired
    private AmenityOrderRepository amenityOrderRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Override
    @Transactional
    public AmenityOrderResponse createOrder(AmenityOrderCreateRequest request) {
        Amenity amenity = amenityRepository.findById(request.getAmenityId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + request.getAmenityId()));

        AmenityOrder order = new AmenityOrder();
        order.setRoomId(request.getRoomId());
        order.setBookingId(request.getBookingId());
        order.setAmenity(amenity);
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");

        AmenityOrder saved = amenityOrderRepository.save(order);
        return mapToResponse(saved);
    }

    @Override
    public List<AmenityOrderResponse> getUnpaidByRoomId(UUID roomId) {
        List<String> unpaidStatuses = Arrays.asList("PENDING", "DELIVERED");
        List<AmenityOrder> orders = amenityOrderRepository.findByRoomIdAndStatusIn(roomId, unpaidStatuses);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AmenityOrderResponse updateOrderStatus(UUID id, String status) {
        AmenityOrder order = amenityOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn dịch vụ với ID: " + id));

        order.setStatus(status.toUpperCase());
        AmenityOrder updated = amenityOrderRepository.save(order);
        return mapToResponse(updated);
    }

    private AmenityOrderResponse mapToResponse(AmenityOrder order) {
        AmenityOrderResponse response = new AmenityOrderResponse();
        response.setId(order.getId());
        response.setRoomId(order.getRoomId());
        response.setBookingId(order.getBookingId());
        response.setAmenityName(order.getAmenity().getName());
        response.setAmenityPrice(order.getAmenity().getPrice());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getAmenity().getPrice().multiply(java.math.BigDecimal.valueOf(order.getQuantity())));
        response.setStatus(order.getStatus());
        return response;
    }
}
