package com.smarthotel.billing.service.impl;

import com.smarthotel.billing.client.AmenityClient;
import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.client.RoomClient;
import com.smarthotel.billing.dto.response.BookingInfoDTO;
import com.smarthotel.billing.dto.response.InvoiceResponse;
import com.smarthotel.billing.dto.response.PaymentInitResponse;
import com.smarthotel.billing.dto.response.RoomInfoDTO;
import com.smarthotel.billing.dto.response.UnpaidAmenityDTO;
import com.smarthotel.billing.entity.Invoice;
import com.smarthotel.billing.entity.InvoiceStatus;
import com.smarthotel.billing.repository.InvoiceRepository;
import com.smarthotel.billing.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");
    private static final String STATUS_BILLED = "BILLED";

    private final InvoiceRepository repo;
    private final BookingClient bookingClient;   // Feign -> booking-service (S2)
    private final RoomClient roomClient;         // Feign -> room-service
    private final AmenityClient amenityClient;   // Feign -> amenities-service (S3)

    public InvoiceServiceImpl(InvoiceRepository repo, BookingClient bookingClient,
                              RoomClient roomClient, AmenityClient amenityClient) {
        this.repo = repo;
        this.bookingClient = bookingClient;
        this.roomClient = roomClient;
        this.amenityClient = amenityClient;
    }

    /**
     * Phuong an B: S5 tu tinh tien phong.
     * Lay roomId + ngay o tu S2 (GET /api/bookings/{id}), gia phong tu room-service
     * (GET /api/rooms/{id}), roi tinh roomCharge = price x so dem.
     */
    @Override
    @Transactional
    public InvoiceResponse generate(UUID bookingId) {
        // Idempotent: goi nhieu lan khong bao loi.
        // - Da PAID -> tra lai nguyen trang.
        // - Chua co / dang UNPAID -> tinh lai tien phong + cong don dich vu moi phat sinh.
        Optional<Invoice> existing = repo.findByBookingId(bookingId);
        if (existing.isPresent() && existing.get().getStatus() == InvoiceStatus.PAID) {
            return InvoiceResponse.from(existing.get());
        }

        BookingInfoDTO booking = bookingClient.getBooking(bookingId);

        // Tien phong = gia mot dem (room-service) x so dem (S2)
        RoomInfoDTO room = roomClient.getRoom(booking.roomId());
        long nights = ChronoUnit.DAYS.between(booking.checkInDate(), booking.checkOutDate());
        if (nights < 1) {
            nights = 1; // toi thieu 1 dem
        }
        BigDecimal roomCharge = room.price().multiply(BigDecimal.valueOf(nights));

        // Chi lay cac order CHUA duoc gop (getUnpaid da loai order BILLED) roi cong don.
        List<UnpaidAmenityDTO> unpaidAmenities = amenityClient.getUnpaid(booking.roomId());
        BigDecimal newService = unpaidAmenities.stream()
                .map(UnpaidAmenityDTO::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice inv = existing.orElseGet(Invoice::new);
        inv.setBookingId(bookingId);
        inv.setRoomCharge(roomCharge);
        BigDecimal prevService = inv.getServiceCharge() == null ? BigDecimal.ZERO : inv.getServiceCharge();
        BigDecimal serviceCharge = prevService.add(newService);
        inv.setServiceCharge(serviceCharge);
        BigDecimal subtotal = roomCharge.add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        inv.setTax(tax);
        inv.setTotalAmount(subtotal.add(tax));
        repo.save(inv);

        // Danh dau cac order vua gop -> BILLED (chong tinh trung lan sau).
        // S3 chi ho tro cap nhat theo tung order id (PUT /orders/{id}/status?status=BILLED).
        for (UnpaidAmenityDTO order : unpaidAmenities) {
            amenityClient.updateOrderStatus(order.id(), STATUS_BILLED);
        }
        return InvoiceResponse.from(inv);
    }

    @Override
    public Invoice findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay hoa don"));
    }

    @Override
    public PaymentInitResponse initPayment(UUID id) {
        Invoice inv = findById(id);
        String qrUrl = "https://img.vietqr.io/image/970415-113366668888-compact2.png"
                + "?amount=" + inv.getTotalAmount().toBigInteger()
                + "&addInfo=INV" + inv.getId();
        return new PaymentInitResponse(qrUrl, inv.getTotalAmount(), "WAITING_BANK");
    }

    @Override
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
