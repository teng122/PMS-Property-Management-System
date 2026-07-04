package com.smarthotel.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthotel.billing.client.AmenityClient;
import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.client.RoomClient;
import com.smarthotel.billing.dto.response.BookingInfoDTO;
import com.smarthotel.billing.dto.response.RoomInfoDTO;
import com.smarthotel.billing.dto.response.UnpaidAmenityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration end-to-end cho S5: chay toan bo luong tren PostgreSQL THAT
 * (Flyway + pgcrypto), cac service ngoai (S2/S3/room) duoc gia lap bang @MockBean
 * cua chinh Feign client (khong con profile mock/gateway).
 *
 * <p>Nguon DB: dung Postgres compose dang chay (localhost:5435) neu co,
 * nguoc lai tu khoi dong Testcontainers PostgreSQL 16.
 * Quy uoc ten *IT -> chay o phase integration-test (failsafe), khong chay o `mvn test`.
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@AutoConfigureMockMvc
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Gia lap cac service ngoai qua Feign client
    @MockBean private BookingClient bookingClient;
    @MockBean private RoomClient roomClient;
    @MockBean private AmenityClient amenityClient;

    @BeforeEach
    void stubDownstream() {
        UUID roomId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        // booking 2 dem
        when(bookingClient.getBooking(any())).thenAnswer(inv -> new BookingInfoDTO(
                inv.getArgument(0), roomId, "Nguyen Van A",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3)));
        // phong 750.000/dem
        when(roomClient.getRoom(any())).thenReturn(new RoomInfoDTO(
                roomId, "101", "DOUBLE", new BigDecimal("750000"), "OCCUPIED"));
        // 1 dich vu 75.000
        when(amenityClient.getUnpaid(any())).thenReturn(List.of(new UnpaidAmenityDTO(
                UUID.randomUUID(), roomId, UUID.randomUUID(), "Buffet",
                new BigDecimal("75000"), 1, new BigDecimal("75000"), "UNPAID")));
    }

    @Test
    void luongDayDu_generate_pay_confirm_get() throws Exception {
        UUID bookingId = UUID.randomUUID();

        // 1) generate -> UNPAID, total 1.732.500
        MvcResult genResult = mockMvc.perform(post("/api/invoices/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":\"" + bookingId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1732500))
                .andExpect(jsonPath("$.status").value("UNPAID"))
                .andReturn();

        JsonNode gen = objectMapper.readTree(genResult.getResponse().getContentAsString());
        assertThat(gen.get("roomCharge").decimalValue()).isEqualByComparingTo("1500000");
        assertThat(gen.get("serviceCharge").decimalValue()).isEqualByComparingTo("75000");
        assertThat(gen.get("tax").decimalValue()).isEqualByComparingTo("157500");

        String id = gen.get("id").asText();

        // 2) pay -> QR VietQR
        mockMvc.perform(post("/api/invoices/{id}/pay", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrImageUrl").value(org.hamcrest.Matchers.containsString("img.vietqr.io")));

        // 3) confirm-payment -> PAID
        mockMvc.perform(post("/api/invoices/{id}/confirm-payment", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // 4) GET -> van la PAID
        mockMvc.perform(get("/api/invoices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
