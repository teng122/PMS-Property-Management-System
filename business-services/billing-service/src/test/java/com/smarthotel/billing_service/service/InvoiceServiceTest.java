package com.smarthotel.billing_service.service;

import com.smarthotel.billing_service.client.AmenityClient;
import com.smarthotel.billing_service.client.BookingClient;
import com.smarthotel.billing_service.client.RoomClient;
import com.smarthotel.billing_service.dto.BookingInfoDTO;
import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.RoomInfoDTO;
import com.smarthotel.billing_service.dto.UnpaidAmenityDTO;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    private InvoiceRepository repo;
    private BookingClient bookingClient;
    private RoomClient roomClient;
    private AmenityClient amenityClient;
    private InvoiceService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(InvoiceRepository.class);
        bookingClient = Mockito.mock(BookingClient.class);
        roomClient = Mockito.mock(RoomClient.class);
        amenityClient = Mockito.mock(AmenityClient.class);
        
        service = new InvoiceService(repo, bookingClient, roomClient, amenityClient);
        // Set the self-injected proxy field to prevent NPE in unit tests
        ReflectionTestUtils.setField(service, "self", service);

        // save returns the invoice entity itself
        when(repo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generate_tinhTienDung_vaMarkBilledMotLan() {
        UUID bookingId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        when(repo.findByBookingId(bookingId)).thenReturn(Optional.empty());

        // Booking: Nguyen Van A, staying for 2 nights, deposit = 0
        BookingInfoDTO booking = new BookingInfoDTO(
                bookingId,
                roomId,
                "Nguyen Van A",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                BigDecimal.ZERO
        );
        when(bookingClient.getBooking(bookingId)).thenReturn(booking);

        // Room: price = 750,000 per night -> 1,500,000 room charge
        RoomInfoDTO room = new RoomInfoDTO(
                roomId,
                "101",
                "DOUBLE",
                BigDecimal.valueOf(750000),
                "AVAILABLE"
        );
        when(roomClient.getRoom(roomId)).thenReturn(room);

        // Amenities: total unpaid service charge = 75,000
        UUID orderId = UUID.randomUUID();
        UnpaidAmenityDTO unpaidAmenity = new UnpaidAmenityDTO(
                orderId,
                roomId,
                bookingId,
                "Spa Service",
                BigDecimal.valueOf(75000),
                1,
                BigDecimal.valueOf(75000),
                "PENDING"
        );
        when(amenityClient.getUnpaid(roomId)).thenReturn(List.of(unpaidAmenity));

        InvoiceResponse resp = service.generate(bookingId);

        assertThat(resp.roomCharge()).isEqualByComparingTo("1500000");
        assertThat(resp.serviceCharge()).isEqualByComparingTo("75000");
        assertThat(resp.tax()).isEqualByComparingTo("157500");
        assertThat(resp.totalAmount()).isEqualByComparingTo("1732500");
        assertThat(resp.status()).isEqualTo("UNPAID");

        // Verify that updateOrderStatus was called for the unpaid order
        verify(amenityClient, times(1)).updateOrderStatus(orderId, "BILLED");
    }

    @Test
    void generate_trungBookingId_nemIllegalState() {
        UUID bookingId = UUID.randomUUID();
        when(repo.findByBookingId(bookingId)).thenReturn(Optional.of(new Invoice()));

        assertThatThrownBy(() -> service.generate(bookingId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markPaid_unpaidChuyenSangPaid_vaPaidAtKhacNull() {
        UUID id = UUID.randomUUID();
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setBookingId(UUID.randomUUID());
        inv.setStatus(InvoiceStatus.UNPAID);
        when(repo.findByIdOrThrow(id)).thenReturn(inv);

        InvoiceResponse resp = service.markPaid(id);

        assertThat(resp.status()).isEqualTo("PAID");
        assertThat(inv.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(inv.getPaidAt()).isNotNull();
    }

    @Test
    void markPaid_hoaDonDaPaid_nemIllegalState() {
        UUID id = UUID.randomUUID();
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setStatus(InvoiceStatus.PAID);
        when(repo.findByIdOrThrow(id)).thenReturn(inv);

        assertThatThrownBy(() -> service.markPaid(id))
                .isInstanceOf(IllegalStateException.class);
    }
}
