package com.smarthotel.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Key;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // chạy SAU CorsFilter (HIGHEST_PRECEDENCE)
public class AuthenticationFilter implements Filter {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 0. CORS preflight: để CorsFilter trả lời, không yêu cầu token
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Skip authentication for public routes
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Validate Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Thiếu hoặc sai định dạng token Authorization\"}");
            return;
        }

        String token = authHeader.substring(7);

        String username;
        String role;
        try {
            // 3. Decode & Validate JWT Token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            username = claims.getSubject();
            role = (String) claims.get("role");
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token không hợp lệ hoặc đã hết hạn\"}");
            return;
        }

        // 4. Wrap request to inject custom headers for downstream services
        CustomHeaderRequestWrapper wrappedRequest = new CustomHeaderRequestWrapper(httpRequest);
        wrappedRequest.putHeader("X-User-Username", username);
        if (role != null) {
            wrappedRequest.putHeader("X-User-Role", role);
        }

        chain.doFilter(wrappedRequest, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/identity-service/api/auth/login") ||
               path.equals("/identity-service/api/auth/register") ||
               path.equals("/identity-service/api/auth/validate") ||
               path.equals("/identity-service/api/auth/refresh") ||
               path.equals("/room-service/api/rooms/search") ||
               path.startsWith("/eureka") ||
               path.startsWith("/actuator") ||
               path.equals("/favicon.ico");
    }

    // Custom HTTP request wrapper to inject custom headers into servlet pipeline
    private static class CustomHeaderRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public CustomHeaderRequestWrapper(HttpServletRequest request) {
            super(request);
            this.customHeaders = new HashMap<>();
        }

        public void putHeader(String name, String value) {
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<>(customHeaders.keySet());
            Enumeration<String> e = super.getHeaderNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return Collections.enumeration(Collections.singletonList(headerValue));
            }
            return super.getHeaders(name);
        }
    }
}
