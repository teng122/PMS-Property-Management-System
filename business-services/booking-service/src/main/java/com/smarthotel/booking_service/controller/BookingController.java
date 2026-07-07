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
 * Các API được sắp xếp theo đúng trình tự vòng đời phục vụ của khách sạn.
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
     * Trạng thái ban đầu sẽ là PENDING và chuyển sang CONFIRMED sau khi giữ phòng thành công.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<BookingResponse> createOnlineBooking(@RequestBody BookingRequest bookingRequest) {
        Booking booking = bookingService.createOnlineBooking(bookingRequest);
        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoomId())
                .customerId(booking.getCustomerId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
    public ResponseEntity<Booking> performWalkInCheckIn(@RequestBody Booking walkInData) {
        Booking newBooking = bookingService.performWalkInCheckIn(walkInData);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    /**
     * Làm thủ tục nhận phòng (Check-in) tại quầy lễ tân cho khách đã đặt phòng trước đó.
     * Xác thực quyền sở hữu (X-User-Id) và chuyển trạng thái đơn đặt phòng từ CONFIRMED sang CHECKED_IN.
     */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Booking> performCheckIn(@PathVariable("id") UUID id) {
        Booking checkedInBooking = bookingService.performCheckIn(id);
        return ResponseEntity.ok(checkedInBooking);
    }

    // ==========================================
    // 3. NHÓM THANH TOÁN & TRẢ PHÒNG (CHECK-OUT & BILLING)
    // ==========================================

    /**
     * Lấy bảng tóm tắt chi phí tạm tính (Pre-checkout Summary) trước khi khách tiến hành check-out thực tế.
     * Thống kê tiền phòng và các dịch vụ đi kèm chưa thanh toán.
     */
    @GetMapping("/{id}/pre-checkout-summary")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<PreCheckoutSummaryResponse> getPreCheckoutSummary(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(bookingService.getPreCheckoutSummary(id));
    }

    /**
     * Làm thủ tục trả phòng (Check-out), giải phóng phòng vật lý sang trạng thái DIRTY và ghi nhận doanh thu.
     * Trạng thái đơn chuyển sang CHECKED_OUT.
     */
    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Booking> performCheckOut(@PathVariable("id") UUID id) {
        Booking checkedOutBooking = bookingService.performCheckOut(id);
        return ResponseEntity.ok(checkedOutBooking);
    }

    // ==========================================
    // 4. NHÓM QUẢN LÝ & TRA CỨU HỆ THỐNG
    // ==========================================

    /**
     * Khách hàng tự lấy danh sách đơn đặt phòng của mình (dựa trên X-User-Id trong token).
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestHeader("X-User-Id") UUID customerId) {
        return ResponseEntity.ok(bookingService.getBookingsByCustomerId(customerId));
    }

    /**
     * Truy vấn thông tin chi tiết một đơn đặt phòng bằng ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Booking> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * Lấy toàn bộ danh sách đơn đặt phòng hiện có trong hệ thống.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * Cập nhật thông tin chi tiết một đơn đặt phòng.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Booking> update(@PathVariable UUID id, @RequestBody Booking updatedData) {
        return ResponseEntity.ok(bookingService.updateBooking(id, updatedData));
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

    @GetMapping("/active-room-ids")
    public ResponseEntity<List<UUID>> getActiveRoomIds(
            @RequestParam("checkIn") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
            @RequestParam("checkOut") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut) {
        List<UUID> activeRoomIds = bookingService.getActiveRoomIds(checkIn, checkOut);
        return ResponseEntity.ok(activeRoomIds);
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
    public ResponseEntity<Booking> payDeposit(
            @PathVariable("id") UUID id,
            @RequestParam("amount") java.math.BigDecimal amount) {
        return ResponseEntity.ok(bookingService.payDeposit(id, amount));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Booking> markNoShow(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(bookingService.markNoShow(id));
    }
}