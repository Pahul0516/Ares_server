package com.ares.ares_server.security;

import com.ares.ares_server.service.AuthService;
import jakarta.servlet.FilterChain;  // ← jakarta, not javax
import jakarta.servlet.ServletException;  // ← jakarta, not javax
import jakarta.servlet.http.HttpServletRequest;  // ← jakarta, not javax
import jakarta.servlet.http.HttpServletResponse;  // ← jakarta, not javax
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 4. Extract username (email) from token
        String email = authService.extractUsername(token);

        // 5. If we got an email and user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Validate the token
            if (authService.isTokenValid(token, email)) {

                // 7. Create authentication object
                // Using empty authorities list since you don't have roles yet
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,  // principal (the user's email)
                                null,   // credentials (we don't need password here)
                                new ArrayList<>()  // authorities/roles (empty for now)
                        );

                // 8. Add request details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Continue with the request
        filterChain.doFilter(request, response);
    }


}
