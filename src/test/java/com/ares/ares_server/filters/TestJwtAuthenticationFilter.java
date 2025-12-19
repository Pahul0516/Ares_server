package com.ares.ares_server.filters;

import com.ares.ares_server.security.JwtAuthenticationFilter;
import com.ares.ares_server.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class TestJwtAuthenticationFilter extends JwtAuthenticationFilter {

    TestJwtAuthenticationFilter(AuthService authService) {
        super(authService);
    }

    public void doFilterInternalPublic(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws Exception {
        super.doFilterInternal(request, response, filterChain);
    }
}
