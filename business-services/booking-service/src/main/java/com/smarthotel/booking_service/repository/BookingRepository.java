package com.smarthotel.booking_service.repository;

import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.exception.BookingNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    default Booking findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id));
    }

    List<Booking> findByCustomerId(UUID customerId);

    @Query("SELECT DISTINCT b.roomId FROM Booking b " +
           "WHERE b.status NOT IN (com.smarthotel.booking_service.entity.BookingStatus.CANCELLED, com.smarthotel.booking_service.entity.BookingStatus.NO_SHOW) " +
           "AND b.checkOutDate > :start AND b.checkInDate < :end")
    List<UUID> findActiveRoomIds(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.roomId = :roomId " +
           "AND b.id != :bookingId " +
           "AND b.status IN (com.smarthotel.booking_service.entity.BookingStatus.AWAITING_DEPOSIT, com.smarthotel.booking_service.entity.BookingStatus.CONFIRMED, com.smarthotel.booking_service.entity.BookingStatus.CHECKED_IN) " +
           "AND b.checkOutDate > :start AND b.checkInDate < :end")
    long countConflictingBookings(
        @Param("roomId") UUID roomId,
        @Param("bookingId") UUID bookingId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}