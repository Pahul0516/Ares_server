package com.ares.ares_server.security;

import com.ares.ares_server.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtChannelInterceptorUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private JwtChannelInterceptor interceptor;

    @Test
    void preSend_connect_withValidJwt_setsAuthenticationInSecurityContext() {
        // GIVEN
        String token = "valid.jwt.token";
        String email = "user@test.com";

        when(authService.extractUsername(token)).thenReturn(email);
        when(authService.isTokenValid(token, email)).thenReturn(true);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + token);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        // WHEN
        interceptor.preSend(message, mock(MessageChannel.class));

        // THEN
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);

        verify(authService).isTokenValid(token, email);
    }

    @Test
    void preSend_connect_withoutValidJwt_setsAuthenticationInSecurityContext() {
        // GIVEN
        String token = "valid.jwt.token";
        String email = "user@test.com";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Be " + token);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        interceptor.preSend(message, mock(MessageChannel.class));
    }

    @Test
    void preSend_connect_withInvalidJwt_throwsException() {
        String token = "invalid.jwt.token";
        String email = "user@test.com";

        when(authService.extractUsername(token)).thenReturn(email);
        when(authService.isTokenValid(token, email)).thenReturn(false);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + token);

        Message<?> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        assertThrows(IllegalArgumentException.class,
                () -> interceptor.preSend(message, mock(MessageChannel.class)));

        verify(authService).extractUsername(token);
        verify(authService).isTokenValid(token, email);
    }

    @Test
    void preSend_connect_withoutAuthorizationHeader_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        Message<?> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNull(resultAccessor.getUser());

        verifyNoInteractions(authService);
    }

    @Test
    void preSend_nonConnectCommand_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setNativeHeader("Authorization", "Bearer token");

        Message<?> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNull(resultAccessor.getUser());

        verifyNoInteractions(authService);
    }

}