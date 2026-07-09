package com.smarthotel.booking_service.controller;

import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.dto.response.PreCheckoutSummaryResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý các API liên quan đến quy trình đặt phòng, check-in, check-out và quản lý Booking.
 * Tất cả endpoint trả về BookingResponse DTO thay vì Booking entity thô.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ==========================================
    // 1. NHÓM ĐẶT PHÒNG (RESERVATION)
    // ==========================================

    /**
     * Khách đặt phòng trực tuyến trước qua ứng dụng di động/website (Saga Workflow).
     * Trạng thái ban đầu sẽ là PENDING và chuyển sang AWAITING_DEPOSIT sau khi giữ phòng thành công.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createOnlineBooking(
            @RequestBody BookingRequest bookingRequest,
            @RequestHeader(value = "X-User-Id") String userIdHeader) {

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu thông tin người dùng trong Token!");
        }
        bookingRequest.setCustomerId(UUID.fromString(userIdHeader));

        Booking booking = bookingService.createOnlineBooking(bookingRequest);
        return new ResponseEntity<>(BookingResponse.from(booking), HttpStatus.CREATED);
    }

    // ==========================================
    // 2. NHÓM NHẬN PHÒNG (CHECK-IN)
    // ==========================================

    /**
     * Đặt phòng và nhận phòng trực tiếp cho khách vãng lai (Walk-in check-in).
     * Trạng thái của đơn đặt phòng sẽ chuyển ngay lập tức sang CHECKED_IN và phòng sẽ được khóa.
     */
    @PostMapping("/walk-in")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> performWalkInCheckIn(@RequestBody Booking walkInData) {
        return new ResponseEntity<>(BookingResponse.from(bookingService.performWalkInCheckIn(walkInData)), HttpStatus.CREATED);
    }

    /**
     * Làm thủ tục nhận phòng (Check-in) tại quầy lễ tân cho khách đã đặt phòng trước đó.
     */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> performCheckIn(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.performCheckIn(id)));
    }

    // ==========================================
    // 3. NHÓM THANH TOÁN & TRẢ PHÒNG (CHECK-OUT & BILLING)
    // ==========================================

    /**
     * Lấy bảng tóm tắt chi phí tạm tính (Pre-checkout Summary) trước khi khách tiến hành check-out thực tế.
     */
    @GetMapping("/{id}/pre-checkout-summary")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<PreCheckoutSummaryResponse> getPreCheckoutSummary(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(bookingService.getPreCheckoutSummary(id));
    }

    /**
     * Làm thủ tục trả phòng (Check-out), giải phóng phòng vật lý sang trạng thái DIRTY và ghi nhận doanh thu.
     */
    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> performCheckOut(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.performCheckOut(id)));
    }

    // ==========================================
    // 4. NHÓM QUẢN LÝ & TRA CỨU HỆ THỐNG
    // ==========================================

    /**
     * Khách hàng tự lấy danh sách đơn đặt phòng của mình (dựa trên X-User-Id trong token).
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@RequestHeader("X-User-Id") UUID customerId) {
        return ResponseEntity.ok(
                bookingService.getBookingsByCustomerId(customerId).stream()
                        .map(BookingResponse::from)
                        .toList()
        );
    }

    /**
     * Truy vấn thông tin chi tiết một đơn đặt phòng bằng ID.
     * CUSTOMER chỉ được xem đơn của chính mình (IDOR / BOLA Prevention).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        Booking booking = bookingService.getBookingById(id);
        if (userRoleHeader != null && userRoleHeader.contains("ROLE_CUSTOMER")) {
            if (userIdHeader == null || !booking.getCustomerId().toString().equalsIgnoreCase(userIdHeader)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập thông tin đơn đặt phòng này!"
                );
            }
        }
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    /**
     * Lấy toàn bộ danh sách đơn đặt phòng hiện có trong hệ thống.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAll() {
        return ResponseEntity.ok(
                bookingService.getAllBookings().stream()
                        .map(BookingResponse::from)
                        .toList()
        );
    }

    /**
     * Cập nhật thông tin chi tiết một đơn đặt phòng.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> update(@PathVariable UUID id, @RequestBody Booking updatedData) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.updateBooking(id, updatedData)));
    }

    /**
     * Xóa một đơn đặt phòng ra khỏi cơ sở dữ liệu hệ thống.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Xóa thành công đơn đặt phòng có ID: " + id);
    }

    // ==========================================
    // 5. NHÓM INTERNAL / FEIGN ENDPOINTS
    // ==========================================

    @GetMapping("/active-room-ids")
    public ResponseEntity<List<UUID>> getActiveRoomIds(
            @RequestParam("checkIn") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
            @RequestParam("checkOut") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut) {
        return ResponseEntity.ok(bookingService.getActiveRoomIds(checkIn, checkOut));
    }

    @GetMapping("/search-available-rooms")
    public ResponseEntity<List<com.smarthotel.booking_service.dto.external.RoomDto>> searchAvailableRooms(
            @RequestParam("checkIn") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
            @RequestParam("checkOut") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut) {
        return ResponseEntity.ok(bookingService.searchAvailableRooms(checkIn, checkOut));
    }

    /**
     * Lấy booking đang hoạt động theo roomId.
     * CUSTOMER chỉ được xem phòng họ đang ở (IDOR / BOLA Prevention).
     */
    @GetMapping("/active/room/{roomId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> getActiveBookingByRoomId(
            @PathVariable("roomId") UUID roomId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        Booking booking = bookingService.getActiveBookingByRoomId(roomId);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        if (userRoleHeader != null && userRoleHeader.contains("ROLE_CUSTOMER")) {
            if (userIdHeader == null || !booking.getCustomerId().toString().equalsIgnoreCase(userIdHeader)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập thông tin đặt phòng của phòng này!"
                );
            }
        }
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    /**
     * Gọi nội bộ (Feign) — vd amenities kiểm tra phòng có khách đang lưu trú trước khi cho gọi dịch vụ.
     * permitAll: không phụ thuộc role của người bấm (amenities tự kiểm quyền sở hữu của CUSTOMER).
     */
    @GetMapping("/internal/active/room/{roomId}")
    public ResponseEntity<BookingResponse> getActiveBookingByRoomIdInternal(@PathVariable("roomId") UUID roomId) {
        Booking booking = bookingService.getActiveBookingByRoomId(roomId);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam("roomId") UUID roomId,
            @RequestParam("bookingId") UUID bookingId,
            @RequestParam("checkIn") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime checkIn,
            @RequestParam("checkOut") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime checkOut) {
        return ResponseEntity.ok(bookingService.checkAvailability(roomId, bookingId, checkIn, checkOut));
    }

    @PostMapping("/{id}/pay-deposit")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> payDeposit(
            @PathVariable("id") UUID id,
            @RequestParam("amount") java.math.BigDecimal amount,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        Booking booking = bookingService.getBookingById(id);
        if (userRoleHeader != null && userRoleHeader.contains("ROLE_CUSTOMER")) {
            if (userIdHeader == null || !booking.getCustomerId().toString().equalsIgnoreCase(userIdHeader)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Bạn không có quyền thanh toán đặt cọc cho đơn đặt phòng này!"
                );
            }
        }
        return ResponseEntity.ok(BookingResponse.from(bookingService.payDeposit(id, amount)));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.markNoShow(id)));
    }
}