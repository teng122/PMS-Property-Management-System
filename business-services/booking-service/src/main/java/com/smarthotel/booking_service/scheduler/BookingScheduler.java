package com.smarthotel.booking_service.scheduler;

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
@Slf4j
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(5); // Hủy sau 5 phút
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(limit);

        if (!expiredBookings.isEmpty()) {
            log.info("[Scheduler] Phát hiện {} đơn đặt phòng quá hạn 5 phút chưa đặt cọc. Tiến hành hủy...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                log.info("[Scheduler] Đã tự động hủy đơn đặt phòng quá hạn: {}", booking.getId());
            }
        }
    }
}
