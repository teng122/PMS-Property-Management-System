package com.smarthotel.common_shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role"); // e.g. ROLE_RECEPTIONIST

        if (userRole != null && !userRole.trim().isEmpty()) {
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(userRole.trim());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId != null ? userId : "anonymous",
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
