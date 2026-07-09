package com.smarthotel.billing_service.service;

import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.PaymentInitResponse;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    private InvoiceRepository repo;
    private InvoiceService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(InvoiceRepository.class);
        service = new InvoiceService(repo);

        // save returns the invoice entity itself
        when(repo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void initPayment_amountNhoDuocDoiSangVndThat() {
        UUID id = UUID.randomUUID();
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setBookingId(UUID.randomUUID());
        inv.setTotalAmount(new BigDecimal("82"));
        when(repo.findByIdOrThrow(id)).thenReturn(inv);

        PaymentInitResponse resp = service.initPayment(id);

        assertThat(resp.amount()).isEqualByComparingTo(new BigDecimal("82000"));
        assertThat(resp.qrImageUrl()).contains("amount=82000");
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
