package com.example.Auth_Service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("DEBUG: RequestLoggingFilter - Incoming Request: " + request.getMethod() + " "
                + request.getRequestURI());
        log.info("Incoming Request: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
