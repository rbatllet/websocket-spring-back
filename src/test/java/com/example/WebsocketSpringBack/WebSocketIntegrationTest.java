package com.example.WebsocketSpringBack;

import com.example.WebsocketSpringBack.config.WebSocketConfig;
import com.example.WebsocketSpringBack.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WebSocket functionality
 * This test starts a server and tests connecting to the WebSocket endpoint
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private String getWebSocketUrl() {
        return "ws://localhost:" + port + "/api/chat";
    }

    @Test
    void contextLoads() {
        // Verify the context loads correctly and our components are injected
        assertNotNull(webSocketConfig);
        assertNotNull(objectMapper);
    }

    @Test
    void testWebSocketConnection() throws Exception {
        // This test will connect to the WebSocket, send a message, and verify the response
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        List<ChatMessage> receivedMessages = new ArrayList<>();
        AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>();

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        
        CompletableFuture<WebSocketSession> sessionFuture = client.execute(
            new TextWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    sessionRef.set(session);
                    connectionLatch.countDown();
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
                    receivedMessages.add(chatMessage);
                    
                    // When we receive a welcome message, count down the latch
                    if (chatMessage.getType() == ChatMessage.MessageType.CHAT &&
                            chatMessage.getMessage().startsWith("Welcome")) {
                        messageLatch.countDown();
                    }
                }
            },
            headers,
            URI.create(getWebSocketUrl())
        );

        // Wait for connection to be established
        assertTrue(connectionLatch.await(5, TimeUnit.SECONDS), "WebSocket connection timed out");
        
        // Wait for welcome message to be received
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Did not receive welcome message");
        
        // Create a synchronized copy of the received messages to avoid ConcurrentModificationException
        List<ChatMessage> messagesCopy;
        synchronized(receivedMessages) {
            messagesCopy = new ArrayList<>(receivedMessages);
        }
        
        // Verify at least one message was received and it's a welcome message
        assertFalse(messagesCopy.isEmpty(), "No messages received");
        
        boolean foundWelcomeMessage = messagesCopy.stream()
            .anyMatch(msg -> msg.getType() == ChatMessage.MessageType.CHAT && 
                     msg.getMessage().startsWith("Welcome"));
                
        assertTrue(foundWelcomeMessage, "Did not receive welcome message");
        
        // Close the session
        WebSocketSession session = sessionFuture.get(1, TimeUnit.SECONDS);
        session.close();
    }
}
