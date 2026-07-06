package com.smarthotel.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthenticationFilterTest {

    @Autowired
    private AuthenticationFilter filter;

    @Value("${jwt.secret}")
    private String secretKeyFromYml;

    @Test
    void testGetSigningKeyWithYmlSecret() {
        // Verify that the secret key injected in the test matches the configuration in application.yml
        assertNotNull(secretKeyFromYml);
        assertFalse(secretKeyFromYml.isEmpty());

        // Access the private method getSigningKey in the Spring-managed bean
        Key key = (Key) ReflectionTestUtils.invokeMethod(filter, "getSigningKey");

        assertNotNull(key);
        assertEquals("HmacSHA", key.getAlgorithm().substring(0, 7));

        // Verify that a token signed with the key can be decoded
        String token = Jwts.builder()
                .setSubject("springbootuser")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        System.out.println("Token is: " + token);

        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertEquals("springbootuser", subject);
    }
}

