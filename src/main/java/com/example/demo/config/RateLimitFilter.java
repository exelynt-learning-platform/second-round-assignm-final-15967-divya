package com.example.demo.config;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 10;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // ✅ Only login API rate limit
        if ("/auth/login".equals(path)) {

            String ip = req.getRemoteAddr();
            String userAgent = req.getHeader("User-Agent");

            // ✅ Improved key (IP + User-Agent)
            String key = ip + "_" + userAgent;

            attempts.putIfAbsent(key, 0);

            if (attempts.get(key) >= MAX_ATTEMPTS) {
                res.setStatus(429);
                res.getWriter().write("Too many login attempts. Try later.");
                return;
            }

            attempts.put(key, attempts.get(key) + 1);
        }

        chain.doFilter(request, response);
    }
}