package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final int MAX_ATTEMPTS = 10;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // 🔥 Only for login API
        if (path.equals("/auth/login")) {

            String ip = req.getRemoteAddr();

            attempts.putIfAbsent(ip, 0);

            if (attempts.get(ip) >= MAX_ATTEMPTS) {
                res.setStatus(429);
                res.getWriter().write("Too many login attempts. Try later.");
                return;
            }

            attempts.put(ip, attempts.get(ip) + 1);
        }

        chain.doFilter(request, response);
    }
}