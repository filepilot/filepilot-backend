package com.filepilot.vcs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
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

    // Only honor X-Forwarded-For / X-Real-IP when behind a trusted proxy (nginx, ALB, Cloudflare).
    // Default false — otherwise any client can spoof a fresh IP per request and bypass the limiter.
    @Value("${app.trusted-proxy:false}")
    private boolean trustProxyHeaders;

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

    /**
     * Periodically drop per-IP entries whose timestamps have all expired. Without this,
     * the map grows unbounded as one-shot IPs accumulate (worse when X-Forwarded-For
     * spoofing is allowed by a misconfigured proxy).
     */
    @Scheduled(fixedDelayString = "${rate.limit.cleanup.interval:300000}")
    public void evictStaleEntries() {
        long cutoff = System.currentTimeMillis() - windowMs;
        Iterator<Map.Entry<String, Deque<Long>>> it = requestCounts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> deque = entry.getValue();
            Long last = deque.peekLast();
            if (last == null || last < cutoff) {
                requestCounts.remove(entry.getKey(), deque);
            }
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustProxyHeaders) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                // Take the first IP (original client), trim whitespace
                return xForwardedFor.split(",")[0].trim();
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank()) {
                return xRealIp.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
