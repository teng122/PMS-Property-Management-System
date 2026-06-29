package com.smarthotel.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration end-to-end: chay toan bo S5 khi S2/S3 CHUA ton tai (profile mock),
 * tren PostgreSQL THAT (can pgcrypto/gen_random_uuid + Flyway, KHONG dung H2).
 *
 * <p>Nguon DB duoc chon linh hoat de test luon XANH:
 * <ul>
 *   <li>Neu da co Postgres dang chay tai localhost:5435 (container postgres-billing
 *       trong docker-compose) -> dung truc tiep.</li>
 *   <li>Nguoc lai -> tu khoi dong Testcontainers PostgreSQL 16 (vd tren CI).</li>
 * </ul>
 * Ca hai deu la Postgres that, chay Flyway V1 + pgcrypto nhu nhau.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class InvoiceFlowIT {

    private static final String RUNNING_DB_HOST = "localhost";
    private static final int RUNNING_DB_PORT = 5435;

    private static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        if (isReachable(RUNNING_DB_HOST, RUNNING_DB_PORT)) {
            // Dung Postgres compose dang chay (postgres-billing)
            registry.add("spring.datasource.url",
                    () -> "jdbc:postgresql://" + RUNNING_DB_HOST + ":" + RUNNING_DB_PORT + "/hotel_billing_db");
            registry.add("spring.datasource.username", () -> "user_billing");
            registry.add("spring.datasource.password", () -> "password123");
        } else {
            // Fallback: Testcontainers PostgreSQL 16 (CI / may co Docker chuan)
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
        assertThat(gen.get("totalAmount").decimalValue()).isEqualByComparingTo("1732500");

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
