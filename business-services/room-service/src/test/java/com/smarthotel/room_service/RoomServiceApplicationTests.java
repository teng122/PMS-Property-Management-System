package com.smarthotel.room_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:postgresql://localhost:5436/hotel_room_db",
		"spring.datasource.username=user_room",
		"spring.datasource.password=password123",
		"spring.datasource.driver-class-name=org.postgresql.Driver",
		"spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false",
		"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
		"spring.jpa.hibernate.ddl-auto=none"
})
class RoomServiceApplicationTests {

	@org.springframework.boot.test.context.TestConfiguration
	static class TestConfig {
		@org.springframework.context.annotation.Bean
		public javax.sql.DataSource dataSource() {
			return org.mockito.Mockito.mock(javax.sql.DataSource.class);
		}
	}

	@org.springframework.boot.test.mock.mockito.MockBean
	private com.smarthotel.room_service.config.RoomDataInitializer roomDataInitializer;

	@Test
	void contextLoads() {
	}

}
