package com.example.demo.config;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${ratelimit.max-attempts}")
    private int maxAttempts;

    @Value("${ratelimit.time-window}")
    private long timeWindow;

    private static final List<String> RATE_LIMITED_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/payment",
            "/orders/place"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (isRateLimitedEndpoint(path)) {

            String key = req.getRemoteAddr();
            String redisKey = "rate_limit:" + path + ":" + key;

            Long attempts = redisTemplate.opsForValue().increment(redisKey);

            if (attempts != null && attempts == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(timeWindow));
            }

            if (attempts != null && attempts > maxAttempts) {
                res.setStatus(429);
                res.getWriter().write("Too many requests. Try later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimitedEndpoint(String path) {
        return RATE_LIMITED_PATHS.stream()
                .anyMatch(path::startsWith);
    }
}