package com.smarthotel.amenities_service.controller;

import com.smarthotel.amenities_service.dto.request.AmenityOrderCreateRequest;
import com.smarthotel.amenities_service.dto.response.AmenityOrderResponse;
import com.smarthotel.amenities_service.service.AmenityOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý luồng gọi dịch vụ phòng (Amenity Orders) của khách hàng.
 * Sắp xếp các API theo đúng chu trình: Gọi dịch vụ -> Cập nhật trạng thái -> Gom hóa đơn dịch vụ chưa trả tiền khi checkout.
 */
@RestController
@RequestMapping("/api/amenities")
public class AmenityOrderController {

    @Autowired
    private AmenityOrderService amenityOrderService;

    // ==========================================
    // 1. GỌI DỊCH VỤ PHÒNG (ORDER LIFECYCLE)
    // ==========================================

    /**
     * Khách hàng tại phòng hoặc lễ tân gọi thêm dịch vụ (ví dụ: ăn sáng, giặt là).
     * Tạo một đơn gọi dịch vụ ở trạng thái PENDING.
     */
    @PostMapping("/order")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<AmenityOrderResponse> createOrder(
            @RequestBody AmenityOrderCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        return ResponseEntity.ok(amenityOrderService.createOrder(request, userIdHeader, userRoleHeader));
    }

    /**
     * Lấy danh sách đơn dịch vụ theo trạng thái (ví dụ: ?status=PENDING,PREPARING) cho màn hàng chờ của bếp/nhân viên dịch vụ.
     * Bỏ trống status = lấy tất cả đơn.
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('STAFF', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<AmenityOrderResponse>> getOrders(
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(amenityOrderService.getOrders(status));
    }

    // ==========================================
    // 2. CẬP NHẬT TIẾN ĐỘ DỊCH VỤ (OPERATIONS)
    // ==========================================

    /**
     * Cập nhật trạng thái phục vụ của đơn dịch vụ phòng (ví dụ: PENDING -> PREPARING -> DELIVERED).
     */
    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<AmenityOrderResponse> updateOrderStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status) {
        return ResponseEntity.ok(amenityOrderService.updateOrderStatus(id, status));
    }

    // ==========================================
    // 3. THÔNG TIN CHECKOUT (BILLING CONNECTIONS)
    // ==========================================

    /**
     * Lấy danh sách toàn bộ các đơn dịch vụ phòng chưa thanh toán (PENDING hoặc DELIVERED) của phòng để gộp vào hóa đơn khi checkout.
     * (Gọi từ Billing Service qua Feign Client).
     */
    @GetMapping("/room/{roomId}/unpaid")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<AmenityOrderResponse>> getUnpaidByRoomId(@PathVariable("roomId") UUID roomId) {
        return ResponseEntity.ok(amenityOrderService.getUnpaidByRoomId(roomId));
    }

    /**
     * Lấy danh sách toàn bộ các đơn dịch vụ phòng chưa thanh toán (PENDING hoặc DELIVERED) của booking để gộp vào hóa đơn khi checkout.
     * (Gọi từ Billing Service qua Feign Client).
     */
    @GetMapping("/booking/{bookingId}/unpaid")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<AmenityOrderResponse>> getUnpaidByBookingId(@PathVariable("bookingId") UUID bookingId) {
        return ResponseEntity.ok(amenityOrderService.getUnpaidByBookingId(bookingId));
    }

    /**
     * Lấy tổng tiền dịch vụ phòng chưa trả cho một đơn đặt phòng (gọi chéo từ Billing Service).
     */
    @GetMapping("/orders/booking/{bookingId}/unpaid-charge")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<java.math.BigDecimal> getUnpaidChargeByBookingId(@PathVariable("bookingId") UUID bookingId) {
        return ResponseEntity.ok(amenityOrderService.getUnpaidChargeByBookingId(bookingId));
    }
}

