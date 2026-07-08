package com.smarthotel.room_booking_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.smarthotel.booking_service.BookingServiceApplication.class, properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:postgresql://localhost:5436/hotel_booking_db",
		"spring.datasource.username=user_booking",
		"spring.datasource.password=password123",
		"spring.datasource.driver-class-name=org.postgresql.Driver",
		"spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false",
		"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
		"spring.jpa.hibernate.ddl-auto=none"
})
class RoomBookingServiceApplicationTests {

	@org.springframework.boot.test.context.TestConfiguration
	static class TestConfig {
		@org.springframework.context.annotation.Bean
		public javax.sql.DataSource dataSource() {
			return org.mockito.Mockito.mock(javax.sql.DataSource.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
