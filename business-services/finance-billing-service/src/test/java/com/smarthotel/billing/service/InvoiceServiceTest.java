package com.smarthotel.billing.service;

import com.smarthotel.billing.domain.Invoice;
import com.smarthotel.billing.domain.InvoiceRepository;
import com.smarthotel.billing.domain.InvoiceStatus;
import com.smarthotel.billing.dto.InvoiceResponse;
import com.smarthotel.billing.gateway.mock.AmenityGatewayMock;
import com.smarthotel.billing.gateway.mock.BookingGatewayMock;
import com.smarthotel.billing.gateway.mock.RoomGatewayMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test THUAN: khong Spring, khong DB.
 * Dung BookingGatewayMock / AmenityGatewayMock that (du lieu mock cua S2/S3),
 * repo duoc gia lap bang Mockito.
 */
class InvoiceServiceTest {

    private InvoiceRepository repo;
    private AmenityGatewayMock amenityGateway;
    private InvoiceService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(InvoiceRepository.class);
        amenityGateway = Mockito.spy(new AmenityGatewayMock());
        // BookingGatewayMock: 2 dem; RoomGatewayMock: 750.000/dem -> roomCharge = 1.500.000
        service = new InvoiceService(repo, new BookingGatewayMock(), new RoomGatewayMock(), amenityGateway);

        // save tra lai chinh entity duoc luu
        when(repo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generate_tinhTienDung_vaMarkBilledMotLan() {
        UUID bookingId = UUID.randomUUID();
        when(repo.findByBookingId(bookingId)).thenReturn(Optional.empty());

        InvoiceResponse resp = service.generate(bookingId);

        assertThat(resp.roomCharge()).isEqualByComparingTo("1500000");
        assertThat(resp.serviceCharge()).isEqualByComparingTo("75000");
        assertThat(resp.tax()).isEqualByComparingTo("157500");
        assertThat(resp.totalAmount()).isEqualByComparingTo("1732500");
        assertThat(resp.status()).isEqualTo("UNPAID");

        // S3 phai duoc dong order dung 1 lan (danh sach order id da gop vao hoa don)
        verify(amenityGateway, times(1)).markBilled(anyList());
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
