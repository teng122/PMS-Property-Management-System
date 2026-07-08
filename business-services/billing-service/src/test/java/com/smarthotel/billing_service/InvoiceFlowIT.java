package com.smarthotel.billing_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@org.junit.jupiter.api.Disabled("Disabled because Docker is not running in the current sandbox environment")
class InvoiceFlowIT {

    private static final String RUNNING_DB_HOST = "localhost";
    private static final int RUNNING_DB_PORT = 5435;

    private static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        if (isReachable(RUNNING_DB_HOST, RUNNING_DB_PORT)) {
            registry.add("spring.datasource.url",
                    () -> "jdbc:postgresql://" + RUNNING_DB_HOST + ":" + RUNNING_DB_PORT + "/hotel_billing_db");
            registry.add("spring.datasource.username", () -> "user_billing");
            registry.add("spring.datasource.password", () -> "password123");
        } else {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("hotel_billing_db")
                    .withUsername("postgres")
                    .withPassword("root");
            postgres.start();
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
    }

    private static boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 800);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void luongDayDu_pay_confirm_get() throws Exception {
        UUID bookingId = UUID.randomUUID();

        // Ghi nhận hóa đơn UNPAID trực tiếp vào database để giả lập event-driven flow
        Invoice invoice = new Invoice();
        invoice.setBookingId(bookingId);
        invoice.setRoomCharge(new BigDecimal("1500000"));
        invoice.setServiceCharge(new BigDecimal("75000"));
        invoice.setTax(new BigDecimal("157500"));
        invoice.setDepositAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(new BigDecimal("1732500"));
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice = invoiceRepository.save(invoice);

        UUID id = invoice.getId();

        // 1) pay -> QR VietQR
        mockMvc.perform(post("/api/invoices/{id}/pay", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrImageUrl").value(org.hamcrest.Matchers.containsString("img.vietqr.io")));

        // 2) confirm-payment -> PAID
        mockMvc.perform(post("/api/invoices/{id}/confirm-payment", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // 3) GET -> van la PAID
        mockMvc.perform(get("/api/invoices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
