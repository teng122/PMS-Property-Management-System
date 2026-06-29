package com.smarthotel.billing.service;

import com.smarthotel.billing.domain.Invoice;
import com.smarthotel.billing.domain.InvoiceRepository;
import com.smarthotel.billing.domain.InvoiceStatus;
import com.smarthotel.billing.dto.BookingInfoDTO;
import com.smarthotel.billing.dto.InvoiceResponse;
import com.smarthotel.billing.dto.PaymentInitResponse;
import com.smarthotel.billing.dto.UnpaidAmenityDTO;
import com.smarthotel.billing.dto.RoomInfoDTO;
import com.smarthotel.billing.gateway.AmenityGateway;
import com.smarthotel.billing.gateway.BookingGateway;
import com.smarthotel.billing.gateway.RoomGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvoiceService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final InvoiceRepository repo;
    private final BookingGateway bookingGateway;   // interface, ban real hoac mock
    private final RoomGateway roomGateway;
    private final AmenityGateway amenityGateway;

    public InvoiceService(InvoiceRepository repo, BookingGateway bg, RoomGateway rg, AmenityGateway ag) {
        this.repo = repo;
        this.bookingGateway = bg;
        this.roomGateway = rg;
        this.amenityGateway = ag;
    }

    /**
     * Phuong an B: S5 tu tinh tien phong.
     * Lay roomId + ngay o tu S2 (GET /api/bookings/{id}), gia phong tu room-service
     * (GET /api/rooms/{id}), roi tinh roomCharge = price x so dem.
     */
    @Transactional
    public InvoiceResponse generate(UUID bookingId) {
        repo.findByBookingId(bookingId).ifPresent(i -> {
            throw new IllegalStateException("Hoa don cho booking nay da ton tai");
        });

        BookingInfoDTO booking = bookingGateway.getBooking(bookingId);

        // Tien phong = gia mot dem (room-service) x so dem (S2)
        RoomInfoDTO room = roomGateway.getRoom(booking.roomId());
        long nights = ChronoUnit.DAYS.between(booking.checkInDate(), booking.checkOutDate());
        if (nights < 1) {
            nights = 1; // toi thieu 1 dem
        }
        BigDecimal roomCharge = room.price().multiply(BigDecimal.valueOf(nights));

        List<UnpaidAmenityDTO> unpaidAmenities = amenityGateway.getUnpaid(booking.roomId());
        BigDecimal serviceCharge = unpaidAmenities.stream()
                .map(UnpaidAmenityDTO::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = roomCharge.add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        Invoice inv = new Invoice();
        inv.setBookingId(bookingId);
        inv.setRoomCharge(roomCharge);
        inv.setServiceCharge(serviceCharge);
        inv.setTax(tax);
        inv.setTotalAmount(subtotal.add(tax));
        repo.save(inv);

        // Dong cac order dich vu vua gop vao hoa don (chong tinh trung)
        List<UUID> billedOrderIds = unpaidAmenities.stream()
                .map(UnpaidAmenityDTO::id)
                .toList();
        amenityGateway.markBilled(billedOrderIds);
        return InvoiceResponse.from(inv);
    }

    public Invoice findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay hoa don"));
    }

    public PaymentInitResponse initPayment(UUID id) {
        Invoice inv = findById(id);
        String qrUrl = "https://img.vietqr.io/image/970415-113366668888-compact2.png"
                + "?amount=" + inv.getTotalAmount().toBigInteger()
                + "&addInfo=INV" + inv.getId();
        return new PaymentInitResponse(qrUrl, inv.getTotalAmount(), "WAITING_BANK");
    }

    @Transactional
    public InvoiceResponse markPaid(UUID id) {
        Invoice inv = findById(id);
        if (inv.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Hoa don da thanh toan");
        }
        inv.setStatus(InvoiceStatus.PAID);
        inv.setPaidAt(Instant.now());
        return InvoiceResponse.from(repo.save(inv));
    }
}
