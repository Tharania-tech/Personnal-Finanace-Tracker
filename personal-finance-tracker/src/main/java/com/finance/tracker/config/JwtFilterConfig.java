package com.finance.tracker.config;

import com.finance.tracker.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class JwtFilterConfig {

    @Bean
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtFilter jwtFilter) {

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

        registration.setFilter(jwtFilter);

        // Apply filter to all API routes
        registration.addUrlPatterns("/api/*");

        registration.setOrder(1);

        return registration;
    }
}