package com.smarthotel.billing.service;

import com.smarthotel.billing.client.AmenityClient;
import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.client.RoomClient;
import com.smarthotel.billing.dto.response.BookingInfoDTO;
import com.smarthotel.billing.dto.response.InvoiceResponse;
import com.smarthotel.billing.dto.response.RoomInfoDTO;
import com.smarthotel.billing.dto.response.UnpaidAmenityDTO;
import com.smarthotel.billing.entity.Invoice;
import com.smarthotel.billing.entity.InvoiceStatus;
import com.smarthotel.billing.repository.InvoiceRepository;
import com.smarthotel.billing.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test THUAN (khong Spring, khong DB) cho {@link InvoiceServiceImpl}.
 * Cac Feign client (S2/S3/room) va repository deu duoc gia lap bang Mockito.
 */
class InvoiceServiceTest {

    private InvoiceRepository repo;
    private BookingClient bookingClient;
    private RoomClient roomClient;
    private AmenityClient amenityClient;
    private InvoiceService service;

    private final UUID roomId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(InvoiceRepository.class);
        bookingClient = Mockito.mock(BookingClient.class);
        roomClient = Mockito.mock(RoomClient.class);
        amenityClient = Mockito.mock(AmenityClient.class);
        service = new InvoiceServiceImpl(repo, bookingClient, roomClient, amenityClient);

        // repo.save tra lai chinh entity duoc luu
        when(repo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** Booking 2 dem, phong 750.000/dem, 1 dich vu 75.000. */
    private UUID stubDownstream(UUID bookingId) {
        UUID orderId = UUID.randomUUID();
        when(bookingClient.getBooking(bookingId)).thenReturn(new BookingInfoDTO(
                bookingId, roomId, "Nguyen Van A",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3)));
        when(roomClient.getRoom(roomId)).thenReturn(new RoomInfoDTO(
                roomId, "101", "DOUBLE", new BigDecimal("750000"), "OCCUPIED"));
        when(amenityClient.getUnpaid(roomId)).thenReturn(List.of(new UnpaidAmenityDTO(
                orderId, roomId, bookingId, "Buffet",
                new BigDecimal("75000"), 1, new BigDecimal("75000"), "UNPAID")));
        return orderId;
    }

    @Test
    void generate_tinhTienDung_vaDanhDauBilled() {
        UUID bookingId = UUID.randomUUID();
        when(repo.findByBookingId(bookingId)).thenReturn(Optional.empty());
        UUID orderId = stubDownstream(bookingId);

        InvoiceResponse resp = service.generate(bookingId);

        assertThat(resp.roomCharge()).isEqualByComparingTo("1500000");   // 750.000 x 2
        assertThat(resp.serviceCharge()).isEqualByComparingTo("75000");
        assertThat(resp.tax()).isEqualByComparingTo("157500");           // 10% x 1.575.000
        assertThat(resp.totalAmount()).isEqualByComparingTo("1732500");
        assertThat(resp.status()).isEqualTo("UNPAID");

        // Order dich vu phai duoc chuyen BILLED dung 1 lan
        verify(amenityClient, times(1)).updateOrderStatus(eq(orderId), eq("BILLED"));
    }

    @Test
    void generate_idempotent_hoaDonDaPaid_traLaiNguyenTrang() {
        UUID bookingId = UUID.randomUUID();
        Invoice paid = new Invoice();
        paid.setId(UUID.randomUUID());
        paid.setBookingId(bookingId);
        paid.setStatus(InvoiceStatus.PAID);
        paid.setRoomCharge(new BigDecimal("1500000"));
        paid.setServiceCharge(new BigDecimal("75000"));
        paid.setTax(new BigDecimal("157500"));
        paid.setTotalAmount(new BigDecimal("1732500"));
        when(repo.findByBookingId(bookingId)).thenReturn(Optional.of(paid));

        InvoiceResponse resp = service.generate(bookingId);

        // Da PAID -> tra lai nguyen trang, KHONG goi downstream, KHONG tao moi
        assertThat(resp.status()).isEqualTo("PAID");
        verify(bookingClient, never()).getBooking(any());
    }

    @Test
    void markPaid_unpaidChuyenSangPaid_vaPaidAtKhacNull() {
        UUID id = UUID.randomUUID();
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setBookingId(UUID.randomUUID());
        inv.setStatus(InvoiceStatus.UNPAID);
        when(repo.findById(id)).thenReturn(Optional.of(inv));

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
        when(repo.findById(id)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.markPaid(id))
                .isInstanceOf(IllegalStateException.class);
    }
}
