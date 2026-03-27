package com.filepilot.vcs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();

    @Value("${rate.limit.requests:60}")
    private int maxRequests;

    @Value("${rate.limit.window:60000}")
    private long windowMs;

    @Value("${rate.limit.auth.requests:10}")
    private int authMaxRequests;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = resolveClientIp(request);
        String path = request.getRequestURI();
        boolean isAuthEndpoint = path.startsWith("/api/auth/");
        int limit = isAuthEndpoint ? authMaxRequests : maxRequests;

        Deque<Long> timestamps = requestCounts.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();

        // Remove expired timestamps
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        timestamps.addLast(now);
        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP (original client), trim whitespace
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
