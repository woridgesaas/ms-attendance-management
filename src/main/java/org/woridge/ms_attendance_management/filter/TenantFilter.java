package org.woridge.ms_attendance_management.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.woridge.ms_attendance_management.dto.tenant.TenantContext;

import java.io.IOException;

@Component
@AllArgsConstructor
public class TenantFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String subdomain = request.getHeader("X-Subdomain");
            if (subdomain != null && !subdomain.isBlank()) {
                TenantContext.setCurrentTenant(subdomain);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }

        filterChain.doFilter(request, response);
    }
}
