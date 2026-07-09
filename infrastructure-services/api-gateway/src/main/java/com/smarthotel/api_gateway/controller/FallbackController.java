package com.smarthotel.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/booking-service")
    public ResponseEntity<Map<String, Object>> bookingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Dịch vụ đặt phòng (booking-service) đang bận hoặc gặp sự cố. Vui lòng thử lại sau!"
                ));
    }

    @GetMapping("/room-service")
    public ResponseEntity<Map<String, Object>> roomFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Dịch vụ phòng (room-service) đang bận hoặc gặp sự cố. Vui lòng thử lại sau!"
                ));
    }

    @GetMapping("/amenities-service")
    public ResponseEntity<Map<String, Object>> amenitiesFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Dịch vụ tiện nghi (amenities-service) đang bận hoặc gặp sự cố. Vui lòng thử lại sau!"
                ));
    }

    @GetMapping("/housekeeping-service")
    public ResponseEntity<Map<String, Object>> housekeepingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Dịch vụ dọn dẹp (housekeeping-service) đang bận hoặc gặp sự cố. Vui lòng thử lại sau!"
                ));
    }

    @GetMapping("/billing-service")
    public ResponseEntity<Map<String, Object>> billingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Dịch vụ thanh toán (billing-service) đang bận hoặc gặp sự cố. Vui lòng thử lại sau!"
                ));
    }
}
