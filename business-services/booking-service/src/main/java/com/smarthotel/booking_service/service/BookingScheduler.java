package com.smarthotel.booking_service.service;

import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    // fixedRate = 60000 có nghĩa là cứ mỗi 60.000 ms (đúng 1 phút) hàm này sẽ tự chạy ngầm
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredBookings() {
        log.info("⏰ [Scheduler] Bắt đầu quét các đơn đặt phòng quá hạn 15 phút...");

        // Tính mốc thời gian: Lấy giờ hiện tại trừ đi 15 phút
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        // Tìm tất cả các đơn PENDING_PAYMENT có thời gian tạo TRƯỚC mốc expirationTime
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, expirationTime);

        // Nếu tìm thấy đơn quá hạn, tiến hành hủy đơn
        if (!expiredBookings.isEmpty()) {
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
                log.info("❌ Đơn đặt phòng ID: {} đã bị HỦY tự động do quá 15 phút chưa thanh toán.", booking.getId());
            }
            // Lưu cập nhật hàng loạt xuống database
            bookingRepository.saveAll(expiredBookings);
        }
    }
}