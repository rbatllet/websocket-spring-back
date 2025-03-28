package com.example.WebsocketSpringBack.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.example.WebsocketSpringBack.ChatMessageHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private ChatMessageHandler chatMessageHandler;
    
    @Value("${websocket.endpoint:/chat}")
    private String endpoint;
    
    @Value("${spring.web.cors.allowed-origins:http://localhost:5173}")
    private String corsAllowedOrigins;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatMessageHandler, endpoint)
                .setAllowedOrigins(corsAllowedOrigins.split(","));
    }
    
    @Value("${websocket.container.max-text-message-buffer-size:8192}")
    private int maxTextMessageBufferSize;
    
    @Value("${websocket.container.max-binary-message-buffer-size:8192}")
    private int maxBinaryMessageBufferSize;
    
    @Value("${websocket.container.max-session-idle-timeout:120000}")
    private long maxSessionIdleTimeout;
    
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(maxTextMessageBufferSize);
        container.setMaxBinaryMessageBufferSize(maxBinaryMessageBufferSize);
        container.setMaxSessionIdleTimeout(maxSessionIdleTimeout);
        return container;
    }
}
