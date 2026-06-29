package com.smarthotel.billing.service;

import com.smarthotel.billing.domain.Invoice;
import com.smarthotel.billing.domain.InvoiceRepository;
import com.smarthotel.billing.domain.InvoiceStatus;
import com.smarthotel.billing.dto.BookingBillingDTO;
import com.smarthotel.billing.dto.InvoiceResponse;
import com.smarthotel.billing.dto.PaymentInitResponse;
import com.smarthotel.billing.dto.UnpaidAmenityDTO;
import com.smarthotel.billing.gateway.AmenityGateway;
import com.smarthotel.billing.gateway.BookingGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvoiceService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final InvoiceRepository repo;
    private final BookingGateway bookingGateway;   // interface, ban real hoac mock
    private final AmenityGateway amenityGateway;

    public InvoiceService(InvoiceRepository repo, BookingGateway bg, AmenityGateway ag) {
        this.repo = repo;
        this.bookingGateway = bg;
        this.amenityGateway = ag;
    }

    @Transactional
    public InvoiceResponse generate(UUID bookingId) {
        repo.findByBookingId(bookingId).ifPresent(i -> {
            throw new IllegalStateException("Hoa don cho booking nay da ton tai");
        });

        BookingBillingDTO booking = bookingGateway.getBillingInfo(bookingId);

        BigDecimal serviceCharge = amenityGateway.getUnpaid(booking.roomId()).stream()
                .map(UnpaidAmenityDTO::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = booking.roomCharge().add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        Invoice inv = new Invoice();
        inv.setBookingId(bookingId);
        inv.setRoomCharge(booking.roomCharge());
        inv.setServiceCharge(serviceCharge);
        inv.setTax(tax);
        inv.setTotalAmount(subtotal.add(tax));
        repo.save(inv);

        amenityGateway.markBilled(booking.roomId());
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
