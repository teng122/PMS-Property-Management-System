package com.smarthotel.billing.web;

import com.smarthotel.billing.dto.InvoiceResponse;
import com.smarthotel.billing.dto.PaymentInitResponse;
import com.smarthotel.billing.service.InvoiceService;
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

/**
 * Web slice test: chi load tang web (InvoiceController + GlobalExceptionHandler),
 * InvoiceService duoc gia lap bang @MockBean.
 */
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService service;

    private static InvoiceResponse sampleInvoice(UUID id, UUID bookingId, String status) {
        return new InvoiceResponse(
                id, bookingId,
                new BigDecimal("1500000"), new BigDecimal("75000"),
                new BigDecimal("157500"), new BigDecimal("1732500"), status);
    }

    @Test
    void generate_tra200_vaJsonDung() throws Exception {
        UUID id = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(service.generate(any(UUID.class))).thenReturn(sampleInvoice(id, bookingId, "UNPAID"));

        mockMvc.perform(post("/api/invoices/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":\"" + bookingId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCharge").value(1500000))
                .andExpect(jsonPath("$.serviceCharge").value(75000))
                .andExpect(jsonPath("$.tax").value(157500))
                .andExpect(jsonPath("$.totalAmount").value(1732500))
                .andExpect(jsonPath("$.status").value("UNPAID"));
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

    @Test
    void generate_khiServiceNemIllegalState_tra409() throws Exception {
        when(service.generate(any(UUID.class)))
                .thenThrow(new IllegalStateException("Hoa don cho booking nay da ton tai"));

        mockMvc.perform(post("/api/invoices/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }
}
