package com.smarthotel.amenities_service.service;

import com.smarthotel.amenities_service.dto.request.AmenityOrderCreateRequest;
import com.smarthotel.amenities_service.dto.response.AmenityOrderResponse;
import com.smarthotel.amenities_service.entity.Amenity;
import com.smarthotel.amenities_service.entity.AmenityOrder;
import com.smarthotel.amenities_service.exception.ResourceNotFoundException;
import com.smarthotel.amenities_service.repository.AmenityOrderRepository;
import com.smarthotel.amenities_service.repository.AmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý chu trình gọi dịch vụ phòng và gộp hóa đơn dịch vụ của khách lưu trú.
 * Các phương thức được sắp xếp theo đúng quy trình thực tế.
 */
@Service
public class AmenityOrderService {

    @Autowired
    private AmenityOrderRepository amenityOrderRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private com.smarthotel.amenities_service.messaging.producer.AmenityOrderEventProducer amenityOrderEventProducer;

    // ==========================================
    // 1. GỌI DỊCH VỤ PHÒNG (ORDER LIFECYCLE)
    // ==========================================

    /**
     * Tạo một đơn gọi dịch vụ phòng mới cho khách đang lưu trú tại khách sạn.
     * Tính toán tổng tiền dựa trên đơn giá danh mục và đặt trạng thái ban đầu là PENDING.
     */
    @Transactional
    public AmenityOrderResponse createOrder(AmenityOrderCreateRequest request) {
        Amenity amenity = amenityRepository.findByIdOrThrow(request.getAmenityId());

        AmenityOrder order = new AmenityOrder();
        order.setRoomId(request.getRoomId());
        order.setBookingId(request.getBookingId());
        order.setStatus(com.smarthotel.amenities_service.entity.AmenityOrderStatus.PENDING);

        java.math.BigDecimal price = amenity.getPrice();
        java.math.BigDecimal totalPrice = price.multiply(java.math.BigDecimal.valueOf(request.getQuantity()));
        order.setTotalPrice(totalPrice);

        // Tạo chi tiết dịch vụ phòng lưu kèm theo Order
        com.smarthotel.amenities_service.entity.AmenityOrderDetail detail = com.smarthotel.amenities_service.entity.AmenityOrderDetail.builder()
                .amenityOrder(order)
                .amenityId(amenity.getId())
                .quantity(request.getQuantity())
                .price(price)
                .build();

        order.getDetails().add(detail);

        AmenityOrder saved = amenityOrderRepository.save(order);

        // Bắn event AmenityOrderCreatedEvent sang Kafka
        com.smarthotel.common_shared.event.AmenityOrderCreatedEvent event = com.smarthotel.common_shared.event.AmenityOrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(saved.getId())
                .bookingId(saved.getBookingId())
                .roomId(saved.getRoomId())
                .totalPrice(saved.getTotalPrice())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        amenityOrderEventProducer.publishOrderCreated(event);

        return mapToResponse(saved);
    }

    // ==========================================
    // 2. CẬP NHẬT TRẠNG THÁI DỊCH VỤ (OPERATIONS)
    // ==========================================

    /**
     * Cập nhật trạng thái xử lý/giao dịch vụ cho phòng (ví dụ: chuyển từ PENDING sang DELIVERED).
     */
    @Transactional
    public AmenityOrderResponse updateOrderStatus(UUID id, String statusStr) {
        AmenityOrder order = amenityOrderRepository.findByIdOrThrow(id);
        com.smarthotel.amenities_service.entity.AmenityOrderStatus newStatus = com.smarthotel.amenities_service.entity.AmenityOrderStatus.valueOf(statusStr.toUpperCase());

        if (newStatus == com.smarthotel.amenities_service.entity.AmenityOrderStatus.REJECTED) {
            // Quy trình hủy/từ chối dịch vụ phòng:
            // 1. Chỉ lễ tân hoặc admin có quyền hủy
            boolean isAuthorized = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST") || a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAuthorized) {
                throw new org.springframework.security.access.AccessDeniedException("Chỉ Lễ tân hoặc Admin mới có quyền hủy dịch vụ!");
            }

            // 2. Kiểm tra trạng thái hủy hợp lệ
            com.smarthotel.amenities_service.entity.AmenityOrderStatus currentStatus = order.getStatus();
            if (currentStatus == com.smarthotel.amenities_service.entity.AmenityOrderStatus.BILLED) {
                throw new IllegalStateException("Không thể hủy dịch vụ đã thanh toán!");
            }
            if (currentStatus == com.smarthotel.amenities_service.entity.AmenityOrderStatus.REJECTED) {
                throw new IllegalStateException("Dịch vụ này đã được hủy từ trước!");
            }

            // Nếu đang chế biến (PREPARING) hoặc đã giao (DELIVERED), kiểm tra tính chất được trả lại (isReturnable)
            if (currentStatus == com.smarthotel.amenities_service.entity.AmenityOrderStatus.PREPARING || currentStatus == com.smarthotel.amenities_service.entity.AmenityOrderStatus.DELIVERED) {
                if (order.getDetails() != null && !order.getDetails().isEmpty()) {
                    UUID amenityId = order.getDetails().iterator().next().getAmenityId();
                    Amenity amenity = amenityRepository.findByIdOrThrow(amenityId);
                    if (!Boolean.TRUE.equals(amenity.getIsReturnable())) {
                        throw new IllegalStateException("Không thể hủy dịch vụ " + amenity.getName() + " khi đang thực hiện hoặc đã giao (loại dịch vụ này không được hoàn trả)!");
                    }
                }
            }
        }

        order.setStatus(newStatus);
        AmenityOrder updated = amenityOrderRepository.save(order);
        return mapToResponse(updated);
    }

    // ==========================================
    // 3. THỐNG KÊ CHECKOUT (BILLING CONNECTIONS)
    // ==========================================

    /**
     * Lấy danh sách đơn dịch vụ theo trạng thái (chuỗi trạng thái cách nhau bởi dấu phẩy, ví dụ: "PENDING,PREPARING").
     * Dùng cho màn hàng chờ chuẩn bị của nhân viên bếp/dịch vụ. Bỏ trống trạng thái = lấy tất cả.
     */
    public List<AmenityOrderResponse> getOrders(String statusCsv) {
        List<AmenityOrder> orders;
        if (statusCsv == null || statusCsv.isBlank()) {
            orders = amenityOrderRepository.findAll();
        } else {
            List<com.smarthotel.amenities_service.entity.AmenityOrderStatus> statuses = Arrays.stream(statusCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> com.smarthotel.amenities_service.entity.AmenityOrderStatus.valueOf(s.toUpperCase()))
                    .collect(Collectors.toList());
            orders = amenityOrderRepository.findByStatusIn(statuses);
        }
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy toàn bộ các dịch vụ phòng chưa thanh toán (ở trạng thái PENDING hoặc DELIVERED) để gộp hóa đơn checkout.
     */
    public List<AmenityOrderResponse> getUnpaidByRoomId(UUID roomId) {
        List<com.smarthotel.amenities_service.entity.AmenityOrderStatus> unpaidStatuses = Arrays.asList(
                com.smarthotel.amenities_service.entity.AmenityOrderStatus.PENDING,
                com.smarthotel.amenities_service.entity.AmenityOrderStatus.DELIVERED
        );
        List<AmenityOrder> orders = amenityOrderRepository.findByRoomIdAndStatusIn(roomId, unpaidStatuses);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tổng tiền của các đơn dịch vụ phòng chưa trả (PENDING, DELIVERED) dựa theo Booking ID.
     */
    public java.math.BigDecimal getUnpaidChargeByBookingId(UUID bookingId) {
        List<com.smarthotel.amenities_service.entity.AmenityOrderStatus> unpaidStatuses = Arrays.asList(
                com.smarthotel.amenities_service.entity.AmenityOrderStatus.PENDING,
                com.smarthotel.amenities_service.entity.AmenityOrderStatus.DELIVERED
        );
        List<AmenityOrder> orders = amenityOrderRepository.findByBookingIdAndStatusIn(bookingId, unpaidStatuses);
        return orders.stream()
                .map(AmenityOrder::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    /**
     * Phương thức phụ trợ chuyển đổi đối tượng Entity AmenityOrder sang DTO Response tương ứng.
     */
    private AmenityOrderResponse mapToResponse(AmenityOrder order) {
        AmenityOrderResponse response = new AmenityOrderResponse();
        response.setId(order.getId());
        response.setRoomId(order.getRoomId());
        response.setBookingId(order.getBookingId());
        response.setStatus(order.getStatus().name());
        response.setTotalPrice(order.getTotalPrice());

        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            com.smarthotel.amenities_service.entity.AmenityOrderDetail detail = order.getDetails().get(0);
            response.setQuantity(detail.getQuantity());
            response.setAmenityPrice(detail.getPrice());

            Amenity amenity = amenityRepository.findById(detail.getAmenityId()).orElse(null);
            if (amenity != null) {
                response.setAmenityName(amenity.getName());
            }
        }
        return response;
    }
}

