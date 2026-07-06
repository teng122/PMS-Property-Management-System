package com.smarthotel.common_shared.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            if (userId != null) {
                template.header("X-User-Id", userId);
            }
            if (userRole != null) {
                template.header("X-User-Role", userRole);
            }
        }
    }
}
