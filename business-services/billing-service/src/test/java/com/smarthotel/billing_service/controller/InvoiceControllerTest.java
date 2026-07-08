package com.smarthotel.billing_service.controller;

import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.PaymentInitResponse;
import com.smarthotel.billing_service.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService service;

    private static InvoiceResponse sampleInvoice(UUID id, UUID bookingId, String status) {
        return new InvoiceResponse(
                id, bookingId,
                new BigDecimal("1500000"), new BigDecimal("75000"),
                new BigDecimal("157500"), BigDecimal.ZERO, new BigDecimal("1732500"), status);
    }

    @Test
    void pay_tra200_vaQrChuaVietQr() throws Exception {
        UUID id = UUID.randomUUID();
        String qr = "https://img.vietqr.io/image/970415-113366668888-compact2.png?amount=1732500&addInfo=INV" + id;
        when(service.initPayment(any(UUID.class)))
                .thenReturn(new PaymentInitResponse(qr, new BigDecimal("1732500"), "WAITING_BANK"));

        mockMvc.perform(post("/api/invoices/{id}/pay", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrImageUrl", containsString("img.vietqr.io")))
                .andExpect(jsonPath("$.state").value("WAITING_BANK"));
    }

    @Test
    void confirmPayment_tra200_statusPaid() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.markPaid(any(UUID.class)))
                .thenReturn(sampleInvoice(id, UUID.randomUUID(), "PAID"));

        mockMvc.perform(post("/api/invoices/{id}/confirm-payment", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
