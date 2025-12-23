package com.ares.ares_server.security;

import com.ares.ares_server.service.AuthService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private FilterChain filterChain;

    private TestJwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new TestJwtAuthenticationFilter(authService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------
    // TESTS
    // -------------------------

    @Test
    void noAuthorizationHeader_shouldSkipAuthentication() throws Exception {

        filter.doFilterInternalPublic(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authService);
    }

    @Test
    void invalidAuthorizationHeader_shouldSkipAuthentication() throws Exception {

        request.addHeader("Authorization", "Basic abc123");

        filter.doFilterInternalPublic(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authService);
    }

    @Test
    void validToken_shouldAuthenticateUser() throws Exception {

        String token = "valid.jwt.token";
        String email = "user@test.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(authService.extractUsername(token)).thenReturn(email);
        when(authService.isTokenValid(token, email)).thenReturn(true);

        filter.doFilterInternalPublic(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(email, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());

        verify(authService).extractUsername(token);
        verify(authService).isTokenValid(token, email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidToken_shouldNotAuthenticate() throws Exception {

        String token = "invalid.token";
        String email = "user@test.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(authService.extractUsername(token)).thenReturn(email);
        when(authService.isTokenValid(token, email)).thenReturn(false);

        filter.doFilterInternalPublic(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenWithNullUsername_shouldSkipAuthentication() throws Exception {

        String token = "token.without.username";
        request.addHeader("Authorization", "Bearer " + token);

        when(authService.extractUsername(token)).thenReturn(null);

        filter.doFilterInternalPublic(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(authService).extractUsername(token);
        verify(authService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nullUsername_andAlreadyAuthenticated_shouldSkipAuthentication() throws Exception {

        String token = "token.without.username";
        String email = "user@test.com";

        // Existing authentication
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(email, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer " + token);

        when(authService.extractUsername(token)).thenReturn(null);

        filter.doFilterInternalPublic(request, response, filterChain);

        // Authentication remains unchanged
        assertSame(
                existingAuth,
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(authService).extractUsername(token);
        verify(authService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void alreadyAuthenticated_shouldSkipJwtProcessing() throws Exception {
        String token = "valid.jwt.token";
        String email = "user@test.com";

        // Pre-set authentication in SecurityContext → makes the condition false
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer " + token);

        when(authService.extractUsername(token)).thenReturn(email);

        filter.doFilterInternalPublic(request, response, filterChain);

        // The existing auth remains unchanged
        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());

        // Token validation is skipped
        verify(authService).extractUsername(token);
        verify(authService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }

}
