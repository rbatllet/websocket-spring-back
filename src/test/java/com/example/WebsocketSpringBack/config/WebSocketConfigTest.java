package com.example.WebsocketSpringBack.config;

import com.example.WebsocketSpringBack.ChatMessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @InjectMocks
    private WebSocketConfig webSocketConfig;

    @Mock
    private ChatMessageHandler chatMessageHandler;

    @Mock
    private WebSocketHandlerRegistry registry;
    
    @Mock
    private WebSocketHandlerRegistration registration;

    @Test
    void registerWebSocketHandlers_shouldRegisterHandlerWithEndpoint() {
        // Arrange
        String endpoint = "/test-chat";
        String corsAllowedOrigins = "http://localhost:5173";
        
        ReflectionTestUtils.setField(webSocketConfig, "endpoint", endpoint);
        ReflectionTestUtils.setField(webSocketConfig, "corsAllowedOrigins", corsAllowedOrigins);
        
        // Mock the registry to return a registration for method chaining
        when(registry.addHandler(any(), anyString())).thenReturn(registration);
        
        // Act
        webSocketConfig.registerWebSocketHandlers(registry);
        
        // Assert
        // Verify that the handler is registered with the correct endpoint
        verify(registry).addHandler(chatMessageHandler, endpoint);
        
        // Verify that the allowed origins are set on the registration (not the registry)
        verify(registration).setAllowedOrigins(corsAllowedOrigins.split(","));
    }
    
    @Test
    void createWebSocketContainer_shouldConfigureCorrectly() {
        // Arrange
        int maxTextMessageBufferSize = 8192;
        int maxBinaryMessageBufferSize = 8192;
        long maxSessionIdleTimeout = 120000L;
        
        ReflectionTestUtils.setField(webSocketConfig, "maxTextMessageBufferSize", maxTextMessageBufferSize);
        ReflectionTestUtils.setField(webSocketConfig, "maxBinaryMessageBufferSize", maxBinaryMessageBufferSize);
        ReflectionTestUtils.setField(webSocketConfig, "maxSessionIdleTimeout", maxSessionIdleTimeout);
        
        // Act
        ServletServerContainerFactoryBean container = webSocketConfig.createWebSocketContainer();
        
        // Assert
        assertEquals(maxTextMessageBufferSize, container.getMaxTextMessageBufferSize());
        assertEquals(maxBinaryMessageBufferSize, container.getMaxBinaryMessageBufferSize());
        assertEquals(maxSessionIdleTimeout, container.getMaxSessionIdleTimeout());
    }
}
