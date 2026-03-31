package com.example.demo.config;

import java.io.IOException;
import java.time.Duration;

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

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 10;
    private static final long TIME_WINDOW = 15 * 60; // 15 minutes

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if ("/auth/login".equals(path)) {

            String key = req.getRemoteAddr();
            String redisKey = "rate_limit:" + key;

            Long attempts = redisTemplate.opsForValue().increment(redisKey);

            if (attempts != null && attempts == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(TIME_WINDOW));
            }

            if (attempts != null && attempts > MAX_ATTEMPTS) {
                res.setStatus(429);
                res.getWriter().write("Too many login attempts. Try later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}